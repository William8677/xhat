package com.williamfq.xhat.ui.Navigation

sealed class Screen(val route: String) {
    object PhoneNumber : Screen("phone_number")
    object VerificationCode : Screen("verification_code/{phoneNumber}/{verificationId}") {
        fun createRoute(phoneNumber: String, verificationId: String) = "verification_code/$phoneNumber/$verificationId"
    }
    object ProfileSetup : Screen("profile_setup")
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object Stories : Screen("stories")
    object AddStory : Screen("add_story")
    object Channels : Screen("channels")
    object Communities : Screen("communities")
    object ChatRooms : Screen("chat_rooms")
    object Calls : Screen("calls")
    object Chats : Screen("chats")
    object ChatList : Screen("chat_list")
    object Chat : Screen("chat/{chatId}") {
        fun createRoute(chatId: String) = "chat/$chatId"
    }
    object ChatDetail : Screen("chat_detail/{chatId}") {
        fun createRoute(chatId: String) = "chat_detail/$chatId"
    }
    object PanicLocation : Screen("panic_location/{chatId}/{chatType}") {
        fun createRoute(chatId: String, chatType: ChatType) = "panic_location/$chatId/${chatType.name}"
    }
}

enum class ChatType {
    ONE_ON_ONE,
    GROUP
}