package com.project.medium.services;

import com.project.medium.model.Blog;

import java.util.List;
import java.util.Optional;

public interface BlogCrudService {
    List<Blog> findAll();

    Optional<Blog> findById(Long id);



    void delete(Blog blog);

    void save(Blog blog);


    Blog increaseLike(Blog blog);

    Blog decreaseLike(Blog blog);

    List<Blog> findAllByCategory_IdAndStatus(Long id,Boolean status);
}
