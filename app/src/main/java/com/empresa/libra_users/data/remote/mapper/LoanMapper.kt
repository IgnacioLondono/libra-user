package com.empresa.libra_users.data.remote.mapper

import com.empresa.libra_users.data.local.user.LoanEntity
import com.empresa.libra_users.data.remote.dto.LoanDto

object LoanMapper {
    
    fun LoanDto.toEntity(): LoanEntity {
        return LoanEntity(
            id = id.toLongOrNull() ?: 0L,
            userId = userId.toLongOrNull() ?: 0L,
            bookId = bookId.toLongOrNull() ?: 0L,
            loanDate = loanDate,
            dueDate = dueDate,
            returnDate = returnDate,
            status = status
        )
    }
}

