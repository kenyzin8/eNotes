package com.example.enotes;

public class ImageData {
    private byte[] mByteArray;
    private int mImageId;

    public ImageData(byte[] byteArray, int imageId) {
        mByteArray = byteArray;
        mImageId = imageId;
    }

    public byte[] getByteArray() {
        return mByteArray;
    }

    public int getImageId() {
        return mImageId;
    }
}
