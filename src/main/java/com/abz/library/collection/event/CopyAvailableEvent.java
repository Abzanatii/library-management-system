package com.abz.library.collection.event;

import com.abz.library.collection.entity.BookCopy;

public record CopyAvailableEvent(BookCopy copy) {
}
