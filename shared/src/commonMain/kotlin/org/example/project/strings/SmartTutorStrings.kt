package org.example.project.strings

object SmartTutorStrings {

    private val language: AppLanguage
        get() = LanguageManager.language.value

    private fun localized(
        english: String,
        greek: String,
    ): String {
        return when (language) {
            AppLanguage.ENGLISH -> english
            AppLanguage.GREEK -> greek
        }
    }

    // ---------------------------------------------------------
    // GENERAL
    // ---------------------------------------------------------

    val yes: String
        get() = localized("Yes", "Ναι")

    val no: String
        get() = localized("No", "Όχι")

    // ---------------------------------------------------------
    // REGISTRATION / LOGIN
    // ---------------------------------------------------------

    val username: String
        get() = localized("Username", "Όνομα χρήστη")

    val login: String
        get() = localized("Login", "Σύνδεση")
    val change_language: String
        get() = when (LanguageManager.language.value) {
            AppLanguage.ENGLISH -> "Change language"
            AppLanguage.GREEK -> "Αλλαγή γλώσσας"
        }
    val confirm_password: String
        get() = localized(
            "Confirm Password",
            "Επιβεβαίωση κωδικού"
        )

    val signup: String
        get() = localized("Sign Up", "Εγγραφή")

    val emailHint: String
        get() = "Email"

    val passwordHint: String
        get() = localized("Password", "Κωδικός")

    val forgot_password: String
        get() = localized(
            "Forgot password?",
            "Ξεχάσατε τον κωδικό σας;"
        )

    // ---------------------------------------------------------
    // RESET PASSWORD
    // ---------------------------------------------------------

    val reset_password: String
        get() = localized(
            "Reset Password",
            "Επαναφορά κωδικού πρόσβασης"
        )

    val submit: String
        get() = localized("Submit", "Υποβολή")

    val back_to_login: String
        get() = localized(
            "Back to Login",
            "Επιστροφή στη σύνδεση"
        )

    val error_message_reset_password: String
        get() = localized(
            "Something went wrong. Please try again. If this email is not registered, you may try again or sign up.",
            "Κάτι πήγε στραβά. Δοκιμάστε ξανά. Εάν αυτή η διεύθυνση ηλεκτρονικού ταχυδρομείου δεν έχει καταχωρηθεί, μπορείτε να προσπαθήσετε ξανά ή να εγγραφείτε."
        )

    val success_message_reset_password: String
        get() = localized(
            "If there is an account with this email, you will receive a recovery message shortly.",
            "Εάν υπάρχει λογαριασμός με αυτήν τη διεύθυνση ηλεκτρονικού ταχυδρομείου, θα λάβετε σύντομα ένα μήνυμα ανάκτησης."
        )

    val reset_info: String
        get() = localized(
            "If an account with this email exists, you will receive an email with instructions to reset your password.",
            "Εάν ο λογαριασμός με αυτήν τη διεύθυνση ηλεκτρονικού ταχυδρομείου υπάρχει, θα λάβετε ένα email με οδηγίες για την επαναφορά του κωδικού πρόσβασής σας."
        )

    // ---------------------------------------------------------
    // ERRORS
    // ---------------------------------------------------------

    val invalid_email: String
        get() = localized(
            "Email must contain '@' and '.'",
            "Το email πρέπει να περιέχει '@' και '.'."
        )

    val username_not_empty: String
        get() = localized(
            "Username cannot be empty",
            "Το όνομα χρήστη δεν μπορεί να είναι κενό"
        )

    val invalid_password: String
        get() = localized(
            "Password must be 8+ characters and include a number and a symbol",
            "Ο κωδικός πρόσβασης πρέπει να έχει 8+ χαρακτήρες και να περιλαμβάνει αριθμό και σύμβολο"
        )

    val password_dismatch: String
        get() = localized(
            "The two passwords do not match",
            "Οι δύο κωδικοί πρόσβασης δεν ταιριάζουν"
        )

    val email_dismatch: String
        get() = localized(
            "The email fields do not match",
            "Τα πεδία του email δεν ταιριάζουν"
        )

    val password_conf_password_not_valid: String
        get() = localized(
            "The confirmation password is not valid",
            "Ο κωδικός επιβεβαίωσης δεν είναι έγκυρος"
        )

    val generic_error: String
        get() = localized(
            "Something went wrong. Please try again.",
            "Κάτι πήγε στραβά. Δοκιμάστε ξανά."
        )

    // ---------------------------------------------------------
    // TUTOR INTRO
    // ---------------------------------------------------------

    val type_your_message: String
        get() = localized(
            "Ask something about this Unit",
            "Ρωτήστε κάτι για αυτήν την Ενότητα"
        )

    val waiting_for_answer: String
        get() = localized(
            "Waiting for an answer",
            "Περιμένοντας μια απάντηση"
        )

    // ---------------------------------------------------------
    // SETTINGS
    // ---------------------------------------------------------

    val settings_title: String
        get() = localized("Settings", "Ρυθμίσεις")

    val change_password_title: String
        get() = localized(
            "Change Password",
            "Αλλαγή κωδικού πρόσβασης"
        )

    val old_password: String
        get() = localized(
            "Old Password",
            "Παλιός κωδικός πρόσβασης"
        )

    val new_password: String
        get() = localized(
            "New Password",
            "Νέος κωδικός πρόσβασης"
        )

    val confirm_password_title: String
        get() = localized(
            "Confirm Password",
            "Επιβεβαίωση κωδικού πρόσβασης"
        )

    val update_password: String
        get() = localized(
            "Update Password",
            "Ενημέρωση κωδικού πρόσβασης"
        )

    val delete_account: String
        get() = localized(
            "Delete Account",
            "Διαγραφή λογαριασμού"
        )

    val delete_button: String
        get() = localized("Delete", "Διαγραφή")

    val delete_account_info: String
        get() = localized(
            "If you delete your account, all your previous chat history and progress will be permanently lost.",
            "Εάν διαγράψετε τον λογαριασμό σας, όλο το προηγούμενο ιστορικό συνομιλιών και η πρόοδός σας θα χαθούν οριστικά."
        )

    val confirm_deletion: String
        get() = localized(
            "Confirm Deletion",
            "Επιβεβαίωση διαγραφής"
        )

    val info_deletion: String
        get() = localized(
            "Are you sure you want to delete your account?",
            "Είστε σίγουροι ότι θέλετε να διαγράψετε τον λογαριασμό σας;"
        )

    // ---------------------------------------------------------
    // CHAT HISTORY
    // ---------------------------------------------------------

    val chat_history_title: String
        get() = localized(
            "Chat History",
            "Ιστορικό συνομιλίας"
        )

    val new_chat: String
        get() = localized(
            "New chat",
            "Νέα συνομιλία"
        )

    val material_title: String
        get() = localized(
            "Material",
            "Υλικό"
        )

    val learning_unit_title: String
        get() = localized(
            "Learning Unit",
            "Διδακτική Ενότητα"
        )

    val unit_title: String
        get() = localized(
            "Unit",
            "Ενότητα"
        )

    val uploaded_file_success_title: String
        get() = localized(
            "Uploaded successfully",
            "Ανέβηκε με επιτυχία"
        )

    val uploaded_file_failure_title: String
        get() = localized(
            "Not uploaded",
            "Δεν έχει ανέβει"
        )
    val pdf_upload_info: String
        get() = when (LanguageManager.language.value) {
            AppLanguage.ENGLISH ->
                "For the best results, upload a PDF with clear, readable content. Low-quality scans or unclear images may not be analyzed correctly by the Tutor."

            AppLanguage.GREEK ->
                "Για καλύτερα αποτελέσματα, ανεβάστε ένα PDF με καθαρό και ευανάγνωστο περιεχόμενο. Σαρώσεις χαμηλής ποιότητας ή δυσδιάκριτες εικόνες ενδέχεται να μην αναλυθούν σωστά από τον Tutor."
        }
    // ---------------------------------------------------------
    // PROGRESS
    // ---------------------------------------------------------

    val my_progress_title: String
        get() = localized(
            "My Progress",
            "Η Πρόοδός μου"
        )

    val exerscize_answer: String
        get() = localized(
            "answers",
            "απαντήσεις"
        )

    val exerscize_sum_total: String
        get() = localized(
            "In total, there have been",
            "Συνολικά έχουν γίνει"
        )

    val exerscize_answers_info: String
        get() = localized(
            "The following information only concerns the exercises you have already answered.",
            "Τα παρακάτω στοιχεία αφορούν μόνο τις ασκήσεις που έχεις ήδη απαντήσει."
        )

    val exerscize_answers_sum: String
        get() = localized(
            "Summary of answered exercises:",
            "Σύνοψη απαντημένων ασκήσεων:"
        )

    val last_answer: String
        get() = localized(
            "Last answer you gave",
            "Τελευταία απάντηση που έδωσες"
        )

    val correct_answer_: String
        get() = localized(
            "The correct answer to the question is:",
            "Η σωστή απάντηση της ερώτησης είναι:"
        )

    val excersize_title: String
        get() = localized(
            "Exercise",
            "Άσκηση"
        )

    val of_which: String
        get() = localized(
            "of which are",
            "εκ των οποίων είναι"
        )

    val correct_and: String
        get() = localized(
            "correct and",
            "σωστές και"
        )

    val wrong_: String
        get() = localized(
            "wrong",
            "λάθος"
        )

    val correct_: String
        get() = localized(
            "Correct",
            "Σωστές"
        )

    val you_answered: String
        get() = localized(
            "You answered",
            "Απάντησες"
        )

    val progress_passed: String
        get() = localized(
            "Passed",
            "Επιτυχία"
        )

    val progress_failed: String
        get() = localized(
            "Failed",
            "Αποτυχία"
        )

    val progress_unit_not_completed: String
        get() = localized(
            "The unit has not been completed yet",
            "Η ενότητα δεν έχει ολοκληρωθεί ακόμη"
        )

    val progress_unit_not_completed_see_answers: String
        get() = localized(
            "To see what you answered and the correct answer, go to \"My Progress\" from the menu.",
            "Για να δεις τι είχες απαντήσει και ποια είναι η σωστή απάντηση, πήγαινε από το μενού στην «Πρόοδό μου»."
        )

    // ---------------------------------------------------------
    // TUTOR
    // ---------------------------------------------------------

    val waiting_for_response: String
        get() = localized(
            "Waiting for a response....",
            "Αναμονή για απάντηση...."
        )
    val large_material_may_take_time: String
        get() = when (LanguageManager.language.value) {
            AppLanguage.ENGLISH ->
                "Large learning materials may take a little longer to process. Please wait while SmartTutor prepares your material."

            AppLanguage.GREEK ->
                "Το μεγάλο εκπαιδευτικό υλικό μπορεί να χρειαστεί λίγο περισσότερο χρόνο για να επεξεργαστεί. Παρακαλώ περιμένετε όσο το SmartTutor προετοιμάζει το υλικό σας."
        }
    val learning_tab: String
        get() = localized(
            "Learning",
            "Μάθηση"
        )

    val exercises_tab: String
        get() = localized(
            "Exercises",
            "Ασκήσεις"
        )

    val exercises_more_button: String
        get() = localized(
            "I want more exercises",
            "Θα ήθελα περισσότερες ασκήσεις"
        )

    val exercises_more_loading: String
        get() = localized(
            "Loading...",
            "Φόρτωση..."
        )

    val next_tab: String
        get() = localized(
            "Next",
            "Επόμενο"
        )

    // ---------------------------------------------------------
    // EXERCISE TAB
    // ---------------------------------------------------------

    val excercise: String
        get() = localized(
            "Exercise",
            "Άσκηση"
        )

    val excercise_write_your_answer: String
        get() = localized(
            "Write your answer...",
            "Γράψε την απάντησή σου..."
        )

    val ask_tutor_about_my_answer: String
        get() = localized(
            "Ask the tutor about your answer",
            "Ρώτα τον Tutor για την απάντησή σου"
        )

    val tutor_answer: String
        get() = localized(
            "Tutor Answer",
            "Η απάντηση του Tutor"
        )

    val hint_tab: String
        get() = localized(
            "Hint",
            "Βοήθεια"
        )

    val explanation_tab: String
        get() = localized(
            "Explanation",
            "Εξήγηση"
        )

    val solution_tab: String
        get() = localized(
            "Solution",
            "Λύση"
        )

    val excersize_level: String
        get() = localized(
            "Difficulty Level",
            "Επίπεδο Δυσκολίας"
        )

    val excersize_easy: String
        get() = localized(
            "Easy",
            "Εύκολο"
        )

    val excersize_medium: String
        get() = localized(
            "Medium",
            "Μέτριο"
        )

    val excersize_hard: String
        get() = localized(
            "Hard",
            "Δύσκολο"
        )

    val grade_: String
        get() = localized(
            "The grade of your latest attempt is",
            "Ο βαθμός της τελευταίας σου προσπάθειας είναι"
        )

    val exersize_has_grades: String
        get() = localized(
            "points",
            "μονάδες"
        )

    val exersize_has: String
        get() = localized(
            "This exercise is worth",
            "Η άσκηση πιάνει"
        )

    val attemp_info_retry: String
        get() = localized(
            "You will be able to answer again after you have answered all the exercises in the unit.",
            "Θα μπορείς να απαντήσεις ξανά αφού απαντήσεις όλες τις ασκήσεις της ενότητας."
        )

    val current_attemp: String
        get() = localized(
            "Current Attempt",
            "Τρέχουσα Προσπάθεια"
        )

    val sum_grade_are: String
        get() = localized(
            "Your attempts are:",
            "Οι προσπάθειές σου είναι:"
        )

    val sum_grade: String
        get() = localized(
            "The total number of attempts you made is:",
            "Οι προσπάθειες που έκανες συνολικά είναι:"
        )

    val info_retry: String
        get() = localized(
            "First you must complete all the exercises in the unit. Then you can answer them again.",
            "Πρώτα πρέπει να ολοκληρώσεις όλες τις ασκήσεις της ενότητας και μετά μπορείς να απαντήσεις ξανά."
        )

    val the_correct_answer_is: String
        get() = localized(
            "The correct answer is",
            "Η σωστή απάντηση είναι"
        )

    val all_answers: String
        get() = localized(
            "Answers:",
            "Απαντήσεις:"
        )

    val total_atempts_title: String
        get() = localized(
            "Total attempts:",
            "Προσπάθειες συνολικά:"
        )

    val attempt_title: String
        get() = localized(
            "Attempt",
            "Προσπάθεια"
        )

    val grade_info: String
        get() = localized(
            "grade",
            "βαθμός"
        )

    val unit_progress_title: String
        get() = localized(
            "Unit progress",
            "Πρόοδος ενότητας"
        )

    val you_answered_of: String
        get() = localized(
            "of",
            "από τις"
        )

    val you_answered_of_answers: String
        get() = localized(
            "exercises",
            "ασκήσεις"
        )

    val answers_left_of_questions: String
        get() = localized(
            "questions",
            "ερωτήσεις"
        )

    val answers_left: String
        get() = localized(
            "Remaining",
            "Μένουν άλλες"
        )

    val must_completed_this_attemp_excersizes: String
        get() = localized(
            "You must first complete all the exercises in the unit. Then you will be able to start a new attempt.",
            "Πρέπει πρώτα να ολοκληρώσεις όλες τις ασκήσεις της ενότητας. Μετά θα μπορείς να κάνεις νέα προσπάθεια."
        )

    val cannot_answer_this_time: String
        get() = localized(
            "You cannot answer again at this time.",
            "Δεν μπορείς να απαντήσεις ξανά αυτή τη στιγμή."
        )

    // ---------------------------------------------------------
// PROGRESS - EXTRA
// ---------------------------------------------------------

    val progress_by_material: String
        get() = localized(
            "Progress by material",
            "Πρόοδος ανά υλικό"
        )

    fun progress_percent(value: Int): String =
        localized(
            "Progress: $value%",
            "Πρόοδος: $value%"
        )

    fun units_read(
        read: Int,
        total: Int,
    ): String =
        localized(
            "$read/$total units read",
            "$read/$total ενότητες διαβασμένες"
        )

    fun exercises_answered(
        answered: Int,
        total: Int,
    ): String =
        localized(
            "$answered/$total exercises answered",
            "$answered/$total ασκήσεις απαντημένες"
        )

// ---------------------------------------------------------
// DELETE MATERIAL
// ---------------------------------------------------------

    val delete_material_title: String
        get() = localized(
            "Delete material?",
            "Διαγραφή υλικού;"
        )

    fun delete_material_info(
        subjectTitle: String,
    ): String =
        localized(
            "This will delete \"$subjectTitle\" and all its units and chat history. This cannot be undone.",
            "Αυτό θα διαγράψει το \"$subjectTitle\", όλες τις ενότητές του και το ιστορικό συνομιλίας. Η ενέργεια αυτή δεν μπορεί να αναιρεθεί."
        )

    val cancel: String
        get() = localized(
            "Cancel",
            "Ακύρωση"
        )

    // ---------------------------------------------------------
// LOADING / EXERCISES
// ---------------------------------------------------------

    fun preparing_learning_material(
        progress: Int,
    ): String =
        localized(
            "Preparing your learning material... $progress%",
            "Προετοιμασία εκπαιδευτικού υλικού... $progress%"
        )

    val generating_exercises: String
        get() = localized(
            "Generating exercises...",
            "Δημιουργία ασκήσεων..."
        )

    val no_exercises_available: String
        get() = localized(
            "No exercises available",
            "Δεν υπάρχουν διαθέσιμες ασκήσεις"
        )

// ---------------------------------------------------------
// ACCESSIBILITY
// ---------------------------------------------------------

    val back: String
        get() = localized(
            "Back",
            "Πίσω"
        )

    val logout: String
        get() = localized(
            "Logout",
            "Αποσύνδεση"
        )

    val progress: String
        get() = localized(
            "Progress",
            "Πρόοδος"
        )


}
