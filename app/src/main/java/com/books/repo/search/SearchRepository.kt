package com.books.repo.search

import android.util.Log
import com.books.api.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SearchRepository @Inject constructor(
    private val apiClient: ApiClient
) {
    companion object {
        private const val TAG = "SearchRepository"
        private const val ITEM_COUNT_PER_PAGE = 10
    }

    private var total: Int = 0
    private var page: Int = 1

    private fun isFull() : Boolean{
        return total <= ((page - 1) * ITEM_COUNT_PER_PAGE)
    }

    suspend fun init() = withContext(Dispatchers.IO) {
        total = 0
        page = 1
    }

    suspend fun searchBook(query: String): List<Book> {
        val bookList: ArrayList<Book> = ArrayList()
            when {
                query.matches(Regex("\\w+\\|\\w+")) -> {
                    val keywords = query.split("|")
                    for (keyword in keywords) {
                        val booksData = apiClient.searchBook(keyword, page)
                        total = booksData.total.toInt()
                        page = booksData.page.toInt()

                        if (!isFull()) {
                            Log.d(TAG, booksData.toString())

                            booksData.books.map { book ->
                                bookList.add(book)
                            }
                        }
                    }
                }
                query.matches(Regex("\\w+-\\w+")) -> {
                    val firstKeyword = query.split("-")[0]
                    val secondKeyword = query.split("-")[1]
                    val booksData = apiClient.searchBook(firstKeyword, page)
                    Log.d(TAG, "total ${booksData.total}")
                    total = booksData.total.toInt()
                    page = booksData.page.toInt()

                    if (!isFull()) {
                        for (book in booksData.books) {
                            Log.d(TAG, "Total ${book.title}")
                        }
                        booksData.books
                            .filterNot { book ->
                                book.title.contains(secondKeyword, ignoreCase = true)
                            }.map { book ->
                                bookList.add(book)
                                Log.d(TAG, "Exclude ${book.title}")
                            }
                    }
                }
                else -> {
                    val booksData = apiClient.searchBook(query, page)
                    Log.d(TAG, booksData.toString())
                    total = booksData.total.toInt()
                    page = booksData.page.toInt()
                    if (!isFull()) {
                        booksData.books.map { book ->
                            bookList.add(book)
                        }
                    }
                }
            }
            page++

        return bookList
    }
}