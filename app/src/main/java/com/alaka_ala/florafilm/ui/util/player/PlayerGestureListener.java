package com.alaka_ala.florafilm.ui.util.player;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;

public class PlayerGestureListener extends GestureDetector.SimpleOnGestureListener {

    private final Activity activity;
    private ExoPlayer player = null;
    private final PlayerView playerView;
    private final AudioManager audioManager;

    // UI elements for feedback
    private LinearLayout centerFeedbackLayout;
    private final ImageView centerFeedbackIcon;
    private final TextView centerFeedbackText;
    private final ProgressBar centerFeedbackProgress;
    private final TextView speed2xText;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Handler seekHandler = new Handler(Looper.getMainLooper());
    private final Handler scrollSeekHandler = new Handler(Looper.getMainLooper());

    // State management to prevent gesture conflicts
    private enum GestureAction { NONE, SCROLL_BRIGHTNESS, SCROLL_VOLUME, SCROLL_SEEK }
    private GestureAction currentAction = GestureAction.NONE;

    private float initialBrightness;
    private int initialVolume;

    private long accumulatedSeekTimeMs = 0;
    private long horizontalSeekTimeMs = 0;
    private boolean isForwardTap;
    private boolean controlsVisible = true;
    private int seekCombo = 0;
    private boolean isNewCombo = false;

    private static final long SCROLL_SEEK_TIMEOUT = 800; // ms

    public PlayerGestureListener(Activity activity, ExoPlayer player, PlayerView playerView,
                                 LinearLayout centerFeedbackLayout, ImageView centerFeedbackIcon,
                                 TextView centerFeedbackText, ProgressBar centerFeedbackProgress,
                                 TextView speed2xText) {
        this.activity = activity;
        this.player = player;
        this.playerView = playerView;
        this.audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        this.centerFeedbackLayout = centerFeedbackLayout;
        this.centerFeedbackIcon = centerFeedbackIcon;
        this.centerFeedbackText = centerFeedbackText;
        this.centerFeedbackProgress = centerFeedbackProgress;
        this.speed2xText = speed2xText;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        // Reset gesture state on new touch event
        currentAction = GestureAction.NONE;
        
        // If a new touch starts before the timeout, it's a combo
        isNewCombo = (horizontalSeekTimeMs != 0);
        if (isNewCombo) {
            seekCombo++;
        }

        // Cancel any pending scroll seek action from a previous gesture sequence
        scrollSeekHandler.removeCallbacks(applyScrollSeekRunnable);

        // Store initial values for scroll gestures
        initialBrightness = activity.getWindow().getAttributes().screenBrightness;
        if (audioManager != null) {
            initialVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        }
        // Must return true to receive subsequent events
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        // Only process tap if no other gesture is active
        if (currentAction != GestureAction.NONE) {
            return false;
        }
        if (playerView.isControllerVisible()) {
            playerView.hideController();
            controlsVisible = false;
        } else {
            playerView.showController();
            controlsVisible = true;
        }
        return true;
    }

    private final Runnable seekRunnable = () -> {
        if (player != null && accumulatedSeekTimeMs != 0) {
            player.seekTo(player.getCurrentPosition() + accumulatedSeekTimeMs);
        }
        accumulatedSeekTimeMs = 0;
        // Hide feedback after seek is performed
        handler.postDelayed(() -> centerFeedbackLayout.setVisibility(View.GONE), 500);
    };

    private final Runnable applyScrollSeekRunnable = () -> {
        if (player != null && horizontalSeekTimeMs != 0) {
            long newPosition = player.getCurrentPosition() + horizontalSeekTimeMs;
            long duration = player.getDuration();
            if (newPosition < 0) newPosition = 0;
            if (newPosition > duration) newPosition = duration;
            player.seekTo(newPosition);
        }
        horizontalSeekTimeMs = 0;
        seekCombo = 0;
        hideFeedback();
    };

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (controlsVisible) {
            return false;
        }
        // Only process tap if no other gesture is active
        if (currentAction != GestureAction.NONE) {
            return false;
        }
        seekHandler.removeCallbacks(seekRunnable);

        boolean currentTapIsForward = e.getX() >= playerView.getWidth() / 2;

        if (accumulatedSeekTimeMs == 0 || isForwardTap != currentTapIsForward) {
            accumulatedSeekTimeMs = 0;
            isForwardTap = currentTapIsForward;
        }

        if (isForwardTap) {
            accumulatedSeekTimeMs += 15000; // 15 seconds
        } else {
            accumulatedSeekTimeMs -= 15000; // -15 seconds
        }

        String seekText = (accumulatedSeekTimeMs > 0 ? "+" : "") + (accumulatedSeekTimeMs / 1000) + "s";
        
        centerFeedbackIcon.setVisibility(View.GONE);
        centerFeedbackProgress.setVisibility(View.GONE);
        centerFeedbackText.setText(seekText);
        centerFeedbackText.setVisibility(View.VISIBLE);
        centerFeedbackLayout.setVisibility(View.VISIBLE);

        seekHandler.postDelayed(seekRunnable, 800);

        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if (controlsVisible) {
            return;
        }
        // Only process long press if no other gesture is active
        if (currentAction != GestureAction.NONE) {
            return;
        }
        player.setPlaybackSpeed(2.0f);
        speed2xText.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (controlsVisible) {
            return false;
        }
        // Determine gesture type only on the first scroll event
        if (currentAction == GestureAction.NONE) {
            if (Math.abs(distanceX) > Math.abs(distanceY)) {
                currentAction = GestureAction.SCROLL_SEEK;
                showSeekFeedback();
            } else {
                // Check for vertical scroll
                if (e1.getX() < playerView.getWidth() / 2) {
                    currentAction = GestureAction.SCROLL_BRIGHTNESS;
                    showBrightnessFeedback();
                } else {
                    currentAction = GestureAction.SCROLL_VOLUME;
                    showVolumeFeedback();
                }
            }
        }

        // Execute action based on the locked-in gesture
        if (currentAction == GestureAction.SCROLL_BRIGHTNESS) {
            handleBrightnessControl(e1, e2);
        } else if (currentAction == GestureAction.SCROLL_VOLUME) {
            handleVolumeControl(e1, e2);
        } else if (currentAction == GestureAction.SCROLL_SEEK) {
            handleHorizontalSeek(distanceX);
        }
        
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // A fling is the end of a scroll, so hide feedback
        if (currentAction == GestureAction.SCROLL_BRIGHTNESS || currentAction == GestureAction.SCROLL_VOLUME) {
            hideFeedback();
            return true;
        }
        return false;
    }

    private void showBrightnessFeedback() {
        centerFeedbackText.setVisibility(View.GONE);
        centerFeedbackIcon.setImageResource(activity.getResources().getIdentifier("ic_brightness", "drawable", activity.getPackageName()));
        centerFeedbackIcon.setVisibility(View.VISIBLE);
        centerFeedbackProgress.setVisibility(View.VISIBLE);
        centerFeedbackLayout.setVisibility(View.VISIBLE);
    }

    private void showVolumeFeedback() {
        centerFeedbackText.setVisibility(View.GONE);
        centerFeedbackIcon.setImageResource(activity.getResources().getIdentifier("ic_volume", "drawable", activity.getPackageName()));
        centerFeedbackIcon.setVisibility(View.VISIBLE);
        centerFeedbackProgress.setVisibility(View.VISIBLE);
        centerFeedbackLayout.setVisibility(View.VISIBLE);
    }
    
    private void showSeekFeedback() {
        centerFeedbackIcon.setVisibility(View.GONE);
        centerFeedbackProgress.setVisibility(View.GONE);
        centerFeedbackText.setVisibility(View.VISIBLE);
        centerFeedbackLayout.setVisibility(View.VISIBLE);
    }

    private void handleBrightnessControl(MotionEvent e1, MotionEvent e2) {
        Window window = activity.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        float deltaY = e1.getY() - e2.getY();
        // Use initialBrightness to make the change linear
        float newBrightness = initialBrightness + (deltaY / playerView.getHeight());

        if (newBrightness < 0.0f) newBrightness = 0.0f;
        if (newBrightness > 1.0f) newBrightness = 1.0f;

        layoutParams.screenBrightness = newBrightness;
        window.setAttributes(layoutParams);
        centerFeedbackProgress.setProgress((int) (newBrightness * 100));
    }

    private void handleVolumeControl(MotionEvent e1, MotionEvent e2) {
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float deltaY = e1.getY() - e2.getY();

        // Use a scaling factor for smoother control
        float deltaVolume = (deltaY / playerView.getHeight()) * maxVolume;
        // Use initialVolume to make the change linear
        float newVolume = initialVolume + deltaVolume;

        if (newVolume < 0) newVolume = 0;
        if (newVolume > maxVolume) newVolume = maxVolume;

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) newVolume, 0);
        centerFeedbackProgress.setProgress((int) ((newVolume / maxVolume) * 100));
    }
    
    private void handleHorizontalSeek(float distanceX) {
        // Cancel any previously posted seek task
        scrollSeekHandler.removeCallbacks(applyScrollSeekRunnable);

        // Add combo bonus only once per new combo gesture
        if (isNewCombo) {
            long bonusMs = seekCombo * 3000L;
            // Add bonus in the direction of the current accumulated seek
            horizontalSeekTimeMs += (horizontalSeekTimeMs >= 0) ? bonusMs : -bonusMs;
            isNewCombo = false; // Consume the combo flag
        }

        // distanceX is negative for right scroll, positive for left scroll.
        // Let's make it so that scrolling right seeks forward.
        long seekChangeMs = (long) (-distanceX * 100); // 1 pixel = 100ms
        horizontalSeekTimeMs += seekChangeMs;

        String seekText = (horizontalSeekTimeMs >= 0 ? "+" : "") + (horizontalSeekTimeMs / 1000) + "s";
        centerFeedbackText.setText(seekText);

        // Post a new delayed task to apply the seek
        scrollSeekHandler.postDelayed(applyScrollSeekRunnable, SCROLL_SEEK_TIMEOUT);
    }

    // This method must be called from the Activity's onTouchEvent on ACTION_UP
    public void onUp(MotionEvent e) {
        // Reset speed on long press release
        if (player.getPlaybackParameters().speed > 1.0f) {
            player.setPlaybackSpeed(1.0f);
            speed2xText.setVisibility(View.GONE);
        }
        
        if (currentAction == GestureAction.SCROLL_SEEK) {
            // Don't seek immediately. The timeout handler will do it.
            // This allows the user to start a new scroll to continue the combo.
        } else if (currentAction == GestureAction.SCROLL_BRIGHTNESS || currentAction == GestureAction.SCROLL_VOLUME) {
            hideFeedback();
        }
    }
    
    private void hideFeedback() {
        handler.postDelayed(() -> centerFeedbackLayout.setVisibility(View.GONE), 500);
    }
}
