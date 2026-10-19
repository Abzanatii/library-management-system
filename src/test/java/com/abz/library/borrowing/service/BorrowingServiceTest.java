package com.abz.library.borrowing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abz.library.borrowing.BorrowingProperties;
import com.abz.library.borrowing.dto.BorrowingResponse;
import com.abz.library.borrowing.entity.Borrowing;
import com.abz.library.borrowing.entity.BorrowingStatus;
import com.abz.library.borrowing.mapper.BorrowingMapper;
import com.abz.library.borrowing.mapper.BorrowingMapperImpl;
import com.abz.library.borrowing.repository.BorrowingRepository;
import com.abz.library.collection.entity.Book;
import com.abz.library.collection.entity.BookCopy;
import com.abz.library.collection.entity.CopyStatus;
import com.abz.library.collection.repository.BookCopyRepository;
import com.abz.library.collection.repository.BookRepository;
import com.abz.library.collection.service.BookCopyService;
import com.abz.library.exception.BusinessRuleException;
import com.abz.library.fine.entity.FineStatus;
import com.abz.library.fine.repository.FineRepository;
import com.abz.library.fine.service.FineService;
import com.abz.library.reservation.Service.ReservationService;
import com.abz.library.user.entity.Role;
import com.abz.library.user.entity.User;
import com.abz.library.user.entity.UserStatus;
import com.abz.library.user.repository.UserRepository;

@ExtendWith (MockitoExtension.class)
public class BorrowingServiceTest{
    private static final long SAVED_BORROW_ID = 71L;

    private BorrowingService borrowingService;
    private BorrowingMapper borrowingMapper = new BorrowingMapperImpl();
    private BorrowingProperties properties;

    @Mock private BorrowingRepository borrowingRepository;
    @Mock private BookRepository bookRepository;
    @Mock private UserRepository userRepository;
    @Mock private FineRepository fineRepository;
    @Mock private BookCopyRepository bookCopyRepository;
    @Mock private BookCopyService bookCopyService;
    @Mock private FineService fineService;
    @Mock private ReservationService reservationService;

    private static final Instant INSTANT = Instant.parse("2026-10-18T10:00:00Z");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 18, 10, 0) ;
    
    @BeforeEach 
    void set(){
        Clock clock = Clock.fixed(INSTANT, ZoneOffset.UTC);
        properties = new BorrowingProperties(5, new BigDecimal("500"), 14, new BigDecimal(10));
        borrowingService = new BorrowingService(borrowingRepository, borrowingMapper, properties, userRepository, bookRepository, fineRepository, bookCopyRepository, bookCopyService, fineService, reservationService, clock);
    }

    @Test 
    @DisplayName ("1. Successfully borrowing an available book")
    void successfulBookCopyBorrow(){
        User customer = createActiveCustomer();
        Book book = createBook();
        BookCopy copy = createAvailableCopy(book);

        givencustomerAndBookExists(customer, book);
        givenNotAlreadyBorrowed(customer, book);
        givenActiveBorrowingBelowMax(customer, 3);
        givenUnpaidFines(customer, "200");
        givenAvailableCopy(book, copy);
        givenSaveBorrowAssignId();

        BorrowingResponse response = borrowingService.borrow(customer.getId(), book.getId());
        verify(bookCopyService).changeTo(copy, CopyStatus.BORROWED);

        ArgumentCaptor<Borrowing> saved = ArgumentCaptor.forClass(Borrowing.class);
        verify(borrowingRepository).save(saved.capture());
        Borrowing borrowing = saved.getValue();
        assertThat(borrowing.getCustomer()).isSameAs(customer);
        assertThat(borrowing.getBookCopy()).isSameAs(copy);
        assertThat(borrowing.getBorrowedAt()).isEqualTo(NOW);
        assertThat(borrowing.getDueDate()).isEqualTo(NOW.plusDays(14));
        assertThat(borrowing.getStatus()).isEqualTo(BorrowingStatus.ACTIVE);
        assertThat(borrowing.getFineAmount()).isEqualByComparingTo(BigDecimal.ZERO);

        assertThat(response.id()).isEqualTo(SAVED_BORROW_ID);
        assertThat(response.status()).isEqualTo(BorrowingStatus.ACTIVE);
        assertThat(response.borrowedAt()).isEqualTo(NOW);
        assertThat(response.dueDate()).isEqualTo(NOW.plusDays(14));
        assertThat(response.customer().id()).isEqualTo(customer.getId());
        assertThat(response.customer().firstName()).isEqualTo(customer.getFirstName());
        assertThat(response.customer().lastName()).isEqualTo(customer.getLastName());
        assertThat(response.bookCopy().id()).isEqualTo(copy.getId());
        assertThat(response.bookCopy().copyNumber()).isEqualTo(copy.getCopyNumber());
        assertThat(response.bookCopy().bookId()).isEqualTo(copy.getBook().getId());
        assertThat(response.bookCopy().isbn()).isEqualTo(copy.getBook().getIsbn());
        assertThat(response.bookCopy().bookTitle()).isEqualTo(copy.getBook().getTitle());

    }

    @Test 
    @DisplayName ("2. Customer already has 5 active borrowings")
    void failedBorrow_customerHasFiveActiveBorrowing(){
        User customer = createActiveCustomer();
        Book book = createBook();

        givencustomerAndBookExists(customer, book);
        givenActiveBorrowingBelowMax(customer, 5);
        
        assertThatThrownBy(() -> borrowingService.borrow(customer.getId(), book.getId()))
        .isInstanceOf(BusinessRuleException.class)
        .hasMessage("Customer is not allowed to borrow over '" + properties.maxBorrow() + "' books");

        verifyNothingBorrowed();
    }

    @Test 
    @DisplayName ("3. Customer has unpaid fines above 500 EGP")
    void failedBorrow_customerHasOverMaxUnpaidFines(){
        User customer = createActiveCustomer();
        Book book = createBook();
        String unpaidFines = "1500";
        givencustomerAndBookExists(customer, book);
        givenUnpaidFines(customer, unpaidFines);

        assertThatThrownBy(() -> borrowingService.borrow(customer.getId(), book.getId()))
        .isInstanceOf(BusinessRuleException.class)
        .hasMessage("Customer above '" + properties.maxFines() + " EGP' is not allowed to borrow books. Customer has unpaid fines: '" + unpaidFines + " EGP'");

        verifyNothingBorrowed();
    }

    @Test 
    @DisplayName ("4. Customer is suspended")
    void failedBorrow_CustomerSuspended(){
        User customer = createActiveCustomer();
        Book book = createBook();
        customer.setStatus(UserStatus.SUSPENDED);
        givencustomerAndBookExists(customer, book);

        assertThatThrownBy(() -> borrowingService.borrow(customer.getId(), book.getId()))
        .isInstanceOf(BusinessRuleException.class)
        .hasMessage("Customer is currently 'SUSPENDED'");

        verifyNothingBorrowed();
    }

    @Test 
    @DisplayName ("5. No physical copy is available")
    void failedBorrow_NoAvailableBookCopy(){
        User customer = createActiveCustomer();
        Book book = createBook();
        givencustomerAndBookExists(customer, book);

        assertThatThrownBy(() -> borrowingService.borrow(customer.getId(), book.getId()))
        .isInstanceOf(BusinessRuleException.class)
        .hasMessage("No available copies");

        verify(bookCopyService, never()).changeTo(any(), any());
        verify(borrowingRepository, never()).save(any());
    }

    @Test 
    @DisplayName ("6. Customer already has the same book borrowed")
    void failedBorrow_CustomerHasSameBookBorrowed(){
        User customer = createActiveCustomer();
        Book book = createBook();
        givencustomerAndBookExists(customer, book);

        when(borrowingRepository.existsByCustomerIdAndBookCopy_Book_IdAndStatusIn(customer.getId(), book.getId(), BorrowingStatus.IS_BORROWED)).thenReturn(true);

        assertThatThrownBy(() -> borrowingService.borrow(customer.getId(), book.getId()))
        .isInstanceOf(BusinessRuleException.class)
        .hasMessage("Customer already has an active/overdue borrowing for this book");

        verifyNothingBorrowed();
    }

    @Test 
    @DisplayName ("7. Successful return of a book without fine")
    void successfulBookCopyReturnNoFine(){
        Borrowing borrowing = createBorrowing(0, BorrowingStatus.ACTIVE);
        
        when(borrowingRepository.findWithDetailsById(borrowing.getId())).thenReturn(Optional.of(borrowing));

        BorrowingResponse response = borrowingService.returnByCustomer(borrowing.getId(), borrowing.getCustomer().getId());

        assertThat(borrowing.getStatus()).isEqualTo(BorrowingStatus.RETURNED);
        assertThat(borrowing.getReturnedAt()).isEqualTo(NOW);
        assertThat(borrowing.getFineAmount()).isEqualTo(BigDecimal.ZERO);

        verify(bookCopyService).changeTo(borrowing.getBookCopy(), CopyStatus.AVAILABLE);
        assertThat(borrowing.getBookCopy().getStatus()).isEqualTo(CopyStatus.AVAILABLE);

        verify(fineService, never()).create(any(), any(), any());

        assertThat(response.id()).isEqualTo(borrowing.getId());
        assertThat(response.status()).isEqualTo(BorrowingStatus.RETURNED);
        assertThat(response.returnedAt()).isEqualTo(NOW);
        assertThat(response.fineAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test 
    @DisplayName ("8. Returning an overdue book and calculating the fine")
    void SuccessfulBookReturnWithFine(){
        Borrowing borrowing = createBorrowing(5, BorrowingStatus.ACTIVE);

        when(borrowingRepository.findWithDetailsById(borrowing.getId())).thenReturn(Optional.of(borrowing));

        BorrowingResponse response = borrowingService.returnByCustomer(borrowing.getId(), borrowing.getCustomer().getId());

        ArgumentCaptor<BigDecimal> fineAmount = ArgumentCaptor.forClass(BigDecimal.class);
        
        verify(fineService).create(ArgumentMatchers.same(borrowing), fineAmount.capture(), ArgumentMatchers.eq(NOW));

        assertThat(fineAmount.getValue()).isEqualByComparingTo("50");
        assertThat(borrowing.getStatus()).isEqualTo(BorrowingStatus.RETURNED);
        assertThat(borrowing.getReturnedAt()).isEqualTo(NOW);
        

        verify(bookCopyService).changeTo(borrowing.getBookCopy(), CopyStatus.AVAILABLE);
        assertThat(borrowing.getBookCopy().getStatus()).isEqualTo(CopyStatus.AVAILABLE);

        assertThat(response.id()).isEqualTo(borrowing.getId());
        assertThat(response.status()).isEqualTo(BorrowingStatus.RETURNED);
        assertThat(response.returnedAt()).isEqualTo(NOW);
        assertThat(response.fineAmount()).isEqualByComparingTo("50");
    }

    //----

    private static User createActiveCustomer(){
        User customer = User.builder()
                        .firstName("Hudz")
                        .lastName("karmin")
                        .email("Hkarmin@gmail.com")
                        .passwordHash("passwordHash")
                        .role(Role.CUSTOMER)
                        .build();
        customer.setId(77L);
        return customer;
    }

    private static BookCopy createAvailableCopy(Book book){
        BookCopy copy = BookCopy.builder()
                        .book(book)
                        .copyNumber("A-003")
                        .build();
        copy.setId(3L);
        return copy;
    }

    private static Book createBook(){
        Book book = Book.builder()
                    .isbn("1234567890123")
                    .title("just-a-book")
                    .build();
        book.setId(7L);
        return book;
    }

    private static Borrowing createBorrowing(int overdueDays, BorrowingStatus status){
        Borrowing borrowing = Borrowing.builder()
            .customer(createActiveCustomer())
            .bookCopy(createAvailableCopy(createBook()))
            .borrowedAt(NOW.minusDays(overdueDays + 14))
            .dueDate(NOW.minusDays(overdueDays))
            .status(status)
            .build();
        borrowing.setId(9L);
        return borrowing;
    }

    private void givencustomerAndBookExists(User customer, Book book){
        when(userRepository.findByIdAndRole(customer.getId(), Role.CUSTOMER)).thenReturn(Optional.of(customer));
        when(bookRepository.existsById(book.getId())).thenReturn(true);
    }

    private void givenNotAlreadyBorrowed(User customer, Book book){
        when(borrowingRepository.existsByCustomerIdAndBookCopy_Book_IdAndStatusIn(customer.getId(), book.getId(), BorrowingStatus.IS_BORROWED)).thenReturn(false);
    }

    private void givenActiveBorrowingBelowMax(User customer, long activeBorrowing){
        when(borrowingRepository.countByCustomerIdAndStatusIn(customer.getId(), BorrowingStatus.IS_BORROWED)).thenReturn(activeBorrowing);
    }

    private void givenUnpaidFines(User customer, String amount){
        when(fineRepository.sumAmountByCustomerIdAndStatus(customer.getId(), FineStatus.UNPAID)).thenReturn(new BigDecimal(amount));
    }

    private void givenAvailableCopy(Book book, BookCopy copy){
        when(bookCopyRepository.findFirstByBookIdAndStatusOrderByCopyNumberAsc(book.getId(), CopyStatus.AVAILABLE)).thenReturn(Optional.of(copy));
    }

    private void givenSaveBorrowAssignId(){
        when(borrowingRepository.save(any(Borrowing.class))).thenAnswer(invo -> {
            Borrowing savedborrowing = invo.getArgument(0);
            savedborrowing.setId(SAVED_BORROW_ID);
            return savedborrowing;
        });
    }

    private void verifyNothingBorrowed(){
        verify(borrowingRepository, never()).save(any());
        verifyNoInteractions(bookCopyService);
    }
}
