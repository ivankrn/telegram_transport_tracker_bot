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

    @Query("select distinct upper(left(name, 1)) letter from Stop order by letter")
    List<Character> getDistinctFirstLettersOfStops();

    Page<Stop> findByNameStartingWithAndType(String letter, Stop.Type stopType, Pageable pageable);
}
