package com.ivankrn.transport_tracker_bot.database;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StopRepository extends CrudRepository<Stop, Long>, PagingAndSortingRepository<Stop, Long> {

    /**
     * Возвращает список первых букв названий остановок.
     *
     * @return Список первых букв названий остановок
     */
    @Query("select distinct upper(left(name, 1)) letter from Stop order by letter")
    List<Character> getDistinctFirstLettersOfStops();

    /**
     * Возвращает список остановок с названиями, начинающимися на указанную букву.
     *
     * @param letter Первая буква названия остановки
     * @param stopType Вид остановки
     * @param pageable Страница
     * @return Список остановок с названиями, начинающимися на указанную букву
     */
    Page<Stop> findByNameStartingWithAndType(String letter, Stop.Type stopType, Pageable pageable);
}
