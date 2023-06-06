package bjit.ursa.bookservice.repository;

import bjit.ursa.bookservice.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<BookEntity , Long> {
    boolean existsByBookNameAndAuthorName(String bookName, String genre);
    //Optional<BookEntity> findByBookNameAndGenre(String bookName, String genre);
}