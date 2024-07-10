package com.bookstore.jpa.services;

import com.bookstore.jpa.dtos.BookRecordDto;
import com.bookstore.jpa.models.AuthorModel;
import com.bookstore.jpa.models.BookModel;
import com.bookstore.jpa.models.PublisherModel;
import com.bookstore.jpa.models.ReviewModel;
import com.bookstore.jpa.repositories.AuthorRepository;
import com.bookstore.jpa.repositories.BookRepository;
import com.bookstore.jpa.repositories.PublisherRepository;
import com.bookstore.jpa.repositories.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final ReviewRepository reviewRepository;

    public BookService(BookRepository bookRepository, AuthorRepository authorRepository,
                       PublisherRepository publisherRepository, ReviewRepository reviewRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.publisherRepository = publisherRepository;
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public List<BookModel> getAllBooks() {
        return bookRepository.findAll();
    }

    @Transactional(readOnly = true)
    public BookModel findBookById(UUID id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Livro não encontrado com ID: " + id));
    }

    @Transactional
    public BookModel saveBook(BookRecordDto bookRecordDto) {
        BookModel bookModel = new BookModel();
        setBookFields(bookModel, bookRecordDto);
        return bookRepository.save(bookModel);
    }

    @Transactional
    public BookModel updateBook(UUID bookId, BookRecordDto bookRecordDto) {
        BookModel bookModel = findBookById(bookId);
        setBookFields(bookModel, bookRecordDto);
        return bookRepository.save(bookModel);
    }

    @Transactional
    public void deleteBook(UUID id) {
        BookModel book = findBookById(id);
        bookRepository.delete(book);
    }

    private void setBookFields(BookModel bookModel, BookRecordDto bookRecordDto) {
        bookModel.setTitle(bookRecordDto.title());
        setPublisher(bookModel, bookRecordDto.publisherId());
        setAuthors(bookModel, bookRecordDto.authorIds());
        setReview(bookModel, bookRecordDto.reviewComment());
    }

    private void setPublisher(BookModel bookModel, UUID publisherId) {
        PublisherModel publisher = publisherRepository.findById(publisherId)
                .orElseGet(() -> {
                    PublisherModel newPublisher = new PublisherModel();
                    newPublisher.setId(publisherId);
                    return publisherRepository.save(newPublisher);
                });
        bookModel.setPublisher(publisher);
    }

    private void setAuthors(BookModel bookModel, Set<UUID> authorIds) {
        Set<AuthorModel> authors = new HashSet<>();
        for (UUID authorId : authorIds) {
            AuthorModel author = authorRepository.findById(authorId)
                    .orElseThrow(() -> new IllegalArgumentException("Autor não encontrado com ID: " + authorId));
            authors.add(author);
        }
        bookModel.setAuthors(authors);
    }

    private void setReview(BookModel bookModel, String reviewComment) {
        if (reviewComment != null && !reviewComment.isEmpty()) {
            ReviewModel review = new ReviewModel();
            review.setComment(reviewComment);
            review.setBook(bookModel);
            bookModel.setReview(review);
            reviewRepository.save(review);
        }
    }
}
