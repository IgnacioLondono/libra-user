
// package com.empresa.libra_users.viewmodel
//
// import androidx.lifecycle.ViewModel
// import androidx.lifecycle.ViewModelProvider
// import com.empresa.libra_users.data.repository.UserRepository
// import com.empresa.libra_users.data.repository.BookRepository
// import com.empresa.libra_users.data.repository.LoanRepository
// import com.empresa.libra_users.data.repository.NotificationRepository
//
// /**
// * Factory personalizada para crear instancias de MainViewModel
// * inyectando los repositorios necesarios manualmente.
//  *
//  * ESTE ARCHIVO YA NO ES NECESARIO GRACIAS A HILT Y PUEDE SER ELIMINADO.
//  * Se ha comentado para resolver errores de compilación sin eliminarlo permanentemente.
// */
// class MainViewModelFactory(
//    private val userRepository: UserRepository,
//    private val bookRepository: BookRepository,
//    private val loanRepository: LoanRepository,
//    private val notificationRepository: NotificationRepository
// ) : ViewModelProvider.Factory {
//
//    @Suppress("UNCHECKED_CAST")
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        return when {
//            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
//                MainViewModel(
//                    userRepository = userRepository,
//                    bookRepository = bookRepository,
//                    loanRepository = loanRepository,
//                    notificationRepository = notificationRepository
//                ) as T
//            }
//            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
//        }
//    }
// }
