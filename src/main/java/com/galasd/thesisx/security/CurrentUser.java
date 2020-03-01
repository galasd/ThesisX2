package com.galasd.thesisx.security;

@FunctionalInterface
public interface CurrentUser {

    UserEntity getUser();
}
