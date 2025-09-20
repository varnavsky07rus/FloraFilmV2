package com.alaka_ala.florafilm.ui.fragments.film.others;

import java.util.ArrayList;

public abstract class TreeItem {
    private final int level;
    private final String title;

    public TreeItem(int level, String title) {
        this.level = level;
        this.title = title;
    }

    public int getLevel() {
        return level;
    }

    public String getTitle() {
        return title;
    }

    public static class FolderItem extends TreeItem {
        // ---> ИЗМЕНЕНО
        private final ArrayList<TreeItem> children;
        private boolean isExpanded;

        // ---> ИЗМЕНЕНО
        public FolderItem(int level, String title, ArrayList<TreeItem> children) {
            super(level, title);
            this.children = children;
            this.isExpanded = false;
        }

        // ---> ИЗМЕНЕНО
        public ArrayList<TreeItem> getChildren() {
            return children;
        }

        public boolean isExpanded() {
            return isExpanded;
        }

        public void setExpanded(boolean expanded) {
            isExpanded = expanded;
        }
    }

    public static class FileItem extends TreeItem {
        public FileItem(int level, String title) {
            super(level, title);
        }
    }
}