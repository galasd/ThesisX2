package com.galasd.thesisx.security;

import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.EntityNotFoundException;

public interface CrudService<T extends AbstractEntity> {

    JpaRepository<T, Long> getRepository();

    default T save(UserEntity currentUser, T entity) {
        return getRepository().saveAndFlush(entity);
    }

    default void delete(UserEntity currentUser, T entity) {
        if (entity == null) {
            throw new EntityNotFoundException();
        }
        getRepository().delete(entity);
    }

    default void delete(UserEntity currentUser, long id) {
        delete(currentUser, load(id));
    }

    default long count() {
        return getRepository().count();
    }

    default T load(long id) {
        T entity = getRepository().findById(id).orElse(null);
        if (entity == null) {
            throw new EntityNotFoundException();
        }
        return entity;
    }

    T createNew(UserEntity currentUser);
}
