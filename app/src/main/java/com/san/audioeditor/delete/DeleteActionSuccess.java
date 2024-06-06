package com.san.audioeditor.delete;


import java.util.List;

/**
 * 删除成功过
 */
public class DeleteActionSuccess {

    List<String> mPaths;//蔡成功的路径

    boolean isAllDelete;//是否全部删除

    public DeleteActionSuccess(boolean isAllDelete, List<String> mPaths) {
        this.isAllDelete = isAllDelete;
        this.mPaths = mPaths;
    }

    public List<String> getPaths() {
        return mPaths;
    }

    public boolean isAllDelete() {
        return isAllDelete;
    }
}
