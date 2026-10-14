package com.abz.library.borrowing.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.abz.library.borrowing.dto.BorrowingResponse;
import com.abz.library.borrowing.entity.Borrowing;
import com.abz.library.collection.entity.BookCopy;
import com.abz.library.user.entity.User;

@Mapper (componentModel = "Spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BorrowingMapper {

    BorrowingResponse.CustomerInfo toCustomerInfo(User customer);

    @Mapping (source = "book.id", target = "bookId")
    @Mapping (source = "book.isbn", target = "isbn")
    @Mapping (source = "book.title", target = "bookTitle")
    BorrowingResponse.BookInfo toBookInfo(BookCopy bookCopy);
    
    BorrowingResponse toResponse(Borrowing borrowing);
}
