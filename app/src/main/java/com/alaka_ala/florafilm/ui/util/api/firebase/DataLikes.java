package com.alaka_ala.florafilm.ui.util.api.firebase;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class DataLikes {

    private final DatabaseReference filmRef;

    public DataLikes(int kinopoisk_id) {
        filmRef = FirebaseDatabase.getInstance().getReference("films/film_" + kinopoisk_id);
    }

    public interface ListenerLikeDislike {
        void onLike(long count);
        void onDislike(long count);
    }

    /**
     * Устанавливает слушатель для получения актуальных данных о лайках/дизлайках.
     */
    public void getLikeDislike(ListenerLikeDislike listenerLikeDislike) {
        filmRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    FilmDataModel film = dataSnapshot.getValue(FilmDataModel.class);
                    if (film != null) {
                        listenerLikeDislike.onLike(film.getLikes_count());
                        listenerLikeDislike.onDislike(film.getDislikes_count());
                    }
                } else {
                    // Если фильма нет в БД, то у него 0 лайков и дизлайков.
                    listenerLikeDislike.onLike(0);
                    listenerLikeDislike.onDislike(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("DataLikes", "Failed to read value.", databaseError.toException());
            }
        });
    }

    /**
     * Атомарно добавляет, изменяет или удаляет лайк/дизлайк пользователя.
     * Если запись о фильме не существует, она будет создана.
     *
     * @param newVote Голос пользователя: "like" или "dislike".
     */
    public void addLikeDislike(String newVote) {
        FirebaseAuth.getInstance().signInAnonymously()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            runLikeDislikeTransaction(user.getUid(), newVote);
                        } else {
                            Log.w("DataLikes", "Anonymous auth failed or user is null.");
                        }
                    } else {
                        Log.w("DataLikes", "signInAnonymously:failure", task.getException());
                    }
                });
    }

    private void runLikeDislikeTransaction(String userId, String newVote) {
        filmRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                FilmDataModel film = mutableData.getValue(FilmDataModel.class);

                // Случай 1: Фильм не существует в БД. Создаем его.
                if (film == null) {
                    film = new FilmDataModel();
                }

                // ВАЖНО: Проверяем, не стала ли карта null после десериализации из Firebase.
                // Это происходит, если у фильма удалили последнюю оценку.
                // Если карта null, инициализируем ее заново.
                if (film.getUser_ratings() == null) {
                    film.setUser_ratings(new HashMap<>());
                }

                // Теперь можно безопасно работать с картой, она никогда не будет null.
                String currentVote = film.getUser_ratings().get(userId);

                // ... остальная логика остается без изменений ...

                if (currentVote == null) {
                    // Пользователь голосует впервые.
                    if (newVote.equals("like")) {
                        film.setLikes_count(film.getLikes_count() + 1);
                    } else {
                        film.setDislikes_count(film.getDislikes_count() + 1);
                    }
                    film.getUser_ratings().put(userId, newVote);

                } else if (currentVote.equals(newVote)) {
                    // Пользователь отменяет свой голос.
                    if (newVote.equals("like")) {
                        film.setLikes_count(film.getLikes_count() - 1);
                    } else {
                        film.setDislikes_count(film.getDislikes_count() - 1);
                    }
                    film.getUser_ratings().remove(userId);

                } else {
                    // Пользователь меняет свой голос.
                    if (newVote.equals("like")) { // Меняет с дизлайка на лайк
                        film.setDislikes_count(film.getDislikes_count() - 1);
                        film.setLikes_count(film.getLikes_count() + 1);
                    } else { // Меняет с лайка на дизлайк
                        film.setLikes_count(film.getLikes_count() - 1);
                        film.setDislikes_count(film.getDislikes_count() + 1);
                    }
                    film.getUser_ratings().put(userId, newVote);
                }

                mutableData.setValue(film);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    Log.e("DataLikes", "Transaction failed: ", error.toException());
                } else {
                    Log.d("DataLikes", "Transaction successful: " + committed);
                }
            }
        });
    }
}