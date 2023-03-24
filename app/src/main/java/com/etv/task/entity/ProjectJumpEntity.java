package com.etv.task.entity;

public class ProjectJumpEntity {

    SceneEntity sceneEntityFrom;
    SceneEntity sceneEntityTo;
    long projectorTime;

    public ProjectJumpEntity() {
    }

    public ProjectJumpEntity(SceneEntity sceneEntityFrom, SceneEntity sceneEntityTo, long projectorTime) {
        this.sceneEntityFrom = sceneEntityFrom;
        this.sceneEntityTo = sceneEntityTo;
        this.projectorTime = projectorTime;
    }

    public SceneEntity getSceneEntityFrom() {
        return sceneEntityFrom;
    }

    public void setSceneEntityFrom(SceneEntity sceneEntityFrom) {
        this.sceneEntityFrom = sceneEntityFrom;
    }

    public SceneEntity getSceneEntityTo() {
        return sceneEntityTo;
    }

    public void setSceneEntityTo(SceneEntity sceneEntityTo) {
        this.sceneEntityTo = sceneEntityTo;
    }

    public long getProjectorTime() {
        return projectorTime;
    }

    public void setProjectorTime(long projectorTime) {
        this.projectorTime = projectorTime;
    }

    @Override
    public String toString() {
        String fromId = "null";
        String toId = "null";
        if (sceneEntityFrom != null) {
            fromId = sceneEntityFrom.getSenceId();
        }
        if (sceneEntityTo != null) {
            toId = sceneEntityTo.getSenceId();
        }
        return "{" +
                "From=" + fromId +
                ",To=" + toId +
                ",Time=" + projectorTime +
                '}';
    }
}
