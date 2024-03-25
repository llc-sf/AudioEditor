package dev.android.player.framework.data.model;

public enum IdType {
        NA(0),
        Artist(1),
        Album(2),
        Playlist(3),
        Genre(4),

        //随机队列，不要在用于区分存储的随机队列与实际队列
        ShuffleSource(-1);

        public final int mId;

        IdType(final int id) {
            mId = id;
        }

        public static IdType getTypeById(int id) {
            for (IdType type : values()) {
                if (type.mId == id) {
                    return type;
                }
            }

            throw new IllegalArgumentException("Unrecognized id: " + id);
        }
    }