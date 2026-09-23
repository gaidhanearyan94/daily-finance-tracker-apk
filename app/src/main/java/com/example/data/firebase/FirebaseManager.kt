package com.example.data.firebase

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.example.R
import com.example.data.CardAccountEntity
import com.example.data.ExpenseEntity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class FirebaseManager(private val context: Context) {

    private val tag = "FirebaseManager"
    private val scope = CoroutineScope(Dispatchers.IO)

    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    val firestore: FirebaseFirestore by lazy {
        try {
            val dbId = context.getString(R.string.firestore_database_id)
            if (dbId.isNotBlank()) {
                FirebaseFirestore.getInstance(FirebaseApp.getInstance(), dbId)
            } else {
                FirebaseFirestore.getInstance()
            }
        } catch (e: Exception) {
            Log.w(tag, "Using default Firestore instance: ${e.message}")
            FirebaseFirestore.getInstance()
        }
    }

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _syncState = MutableStateFlow(SyncState.IDLE)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime: StateFlow<Long?> = _lastSyncTime.asStateFlow()

    private val _syncMessage = MutableStateFlow("")
    val syncMessage: StateFlow<String> = _syncMessage.asStateFlow()

    // Phone Auth states
    var phoneVerificationId: String? = null
        private set
    var phoneResendToken: PhoneAuthProvider.ForceResendingToken? = null
        private set

    private var expenseListenerRegistration: ListenerRegistration? = null
    private var cardListenerRegistration: ListenerRegistration? = null

    init {
        try {
            _currentUser.value = auth.currentUser
            auth.addAuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                _currentUser.value = user
                if (user != null) {
                    _syncMessage.value = "Connected as ${user.email ?: user.phoneNumber ?: user.displayName ?: "User"}"
                } else {
                    _syncMessage.value = "Signed out"
                    detachCloudListeners()
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize Firebase Auth: ${e.message}")
        }
    }

    // ----------------------------------------------------
    // EMAIL & PASSWORD AUTHENTICATION
    // ----------------------------------------------------

    fun signInWithEmail(
        email: String,
        pass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isBlank() || pass.isBlank()) {
            onError("Please enter both email and password")
            return
        }
        auth.signInWithEmailAndPassword(email.trim(), pass)
            .addOnSuccessListener {
                _currentUser.value = auth.currentUser
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(tag, "Email Sign In error", e)
                onError(e.localizedMessage ?: "Failed to sign in. Please verify credentials.")
            }
    }

    fun signUpWithEmail(
        email: String,
        pass: String,
        displayName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isBlank() || pass.isBlank()) {
            onError("Please enter both email and password")
            return
        }
        if (pass.length < 6) {
            onError("Password must be at least 6 characters")
            return
        }
        auth.createUserWithEmailAndPassword(email.trim(), pass)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (displayName.isNotBlank() && user != null) {
                    val profileUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName.trim())
                        .build()
                    user.updateProfile(profileUpdate).addOnCompleteListener {
                        _currentUser.value = auth.currentUser
                        onSuccess()
                    }
                } else {
                    _currentUser.value = user
                    onSuccess()
                }
            }
            .addOnFailureListener { e ->
                Log.e(tag, "Email Sign Up error", e)
                onError(e.localizedMessage ?: "Failed to create account.")
            }
    }

    fun sendPasswordReset(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (email.isBlank()) {
            onError("Please enter your registered email address")
            return
        }
        auth.sendPasswordResetEmail(email.trim())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.localizedMessage ?: "Failed to send reset email") }
    }

    // ----------------------------------------------------
    // PHONE AUTHENTICATION
    // ----------------------------------------------------

    fun startPhoneNumberVerification(
        activity: Activity,
        phoneNumber: String,
        onCodeSent: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (phoneNumber.isBlank() || phoneNumber.length < 7) {
            onError("Please enter a valid phone number with country code (e.g. +1 555 123 4567)")
            return
        }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-retrieval or instant verification
                auth.signInWithCredential(credential)
                    .addOnSuccessListener {
                        _currentUser.value = auth.currentUser
                        onCodeSent()
                    }
                    .addOnFailureListener { e ->
                        onError(e.localizedMessage ?: "Verification failed")
                    }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e(tag, "Phone verification failed", e)
                onError(e.localizedMessage ?: "SMS verification failed. Check phone format or enable Phone Provider in Firebase console.")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                phoneVerificationId = verificationId
                phoneResendToken = token
                onCodeSent()
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber.trim())
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyPhoneCode(
        smsCode: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val vId = phoneVerificationId
        if (vId.isNullOrBlank()) {
            onError("Verification session expired. Please request a new code.")
            return
        }
        if (smsCode.isBlank() || smsCode.length < 6) {
            onError("Please enter the 6-digit verification code sent via SMS")
            return
        }

        val credential = PhoneAuthProvider.getCredential(vId, smsCode.trim())
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                _currentUser.value = auth.currentUser
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(tag, "SMS code verification failed", e)
                onError(e.localizedMessage ?: "Invalid verification code.")
            }
    }

    // ----------------------------------------------------
    // GOOGLE SIGN-IN VIA CREDENTIAL MANAGER
    // ----------------------------------------------------

    suspend fun signInWithGoogleCredentialManager(
        activity: Activity,
        serverClientId: String? = null
    ): Result<FirebaseUser> {
        return try {
            val credentialManager = CredentialManager.create(activity)
            
            // If server client id is not provided, try to find an OAuth web client or notify
            val clientId = serverClientId?.takeIf { it.isNotBlank() }
                ?: "440207559351-web-client.apps.googleusercontent.com" // Project number reference

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(clientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(activity, request)
            val credential = response.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val user = authResult.user ?: throw IllegalStateException("Firebase user was null after Google sign-in")
                _currentUser.value = user
                Result.success(user)
            } else {
                Result.failure(IllegalArgumentException("Unexpected credential type: ${credential.type}"))
            }
        } catch (e: Exception) {
            Log.e(tag, "Google Sign-In error", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        try {
            detachCloudListeners()
            auth.signOut()
            _currentUser.value = null
            _syncState.value = SyncState.IDLE
            _syncMessage.value = "Signed out"
        } catch (e: Exception) {
            Log.e(tag, "Sign out error", e)
        }
    }

    // ----------------------------------------------------
    // FIRESTORE SYNC & PERSISTENCE
    // ----------------------------------------------------

    fun syncAllToCloud(
        expenses: List<ExpenseEntity>,
        cards: List<CardAccountEntity>,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val user = auth.currentUser
        if (user == null) {
            _syncState.value = SyncState.OFFLINE
            _syncMessage.value = "Sign in to backup to Firebase project personal-finance-tracker-8a00c"
            onComplete(false, "Not signed in")
            return
        }

        scope.launch {
            _syncState.value = SyncState.SYNCING
            _syncMessage.value = "Syncing with Firestore..."
            try {
                val uid = user.uid
                val batch = firestore.batch()

                // Sync expenses
                val expensesCol = firestore.collection("users").document(uid).collection("expenses")
                for (exp in expenses) {
                    val docRef = expensesCol.document(exp.id.toString())
                    batch.set(docRef, FirestoreExpense.fromEntity(exp), SetOptions.merge())
                }

                // Sync cards
                val cardsCol = firestore.collection("users").document(uid).collection("cards")
                for (card in cards) {
                    val docRef = cardsCol.document(card.id.toString())
                    batch.set(docRef, FirestoreCard.fromEntity(card), SetOptions.merge())
                }

                // Metadata
                val metaDoc = firestore.collection("users").document(uid)
                val meta = mapOf(
                    "lastSync" to System.currentTimeMillis(),
                    "email" to (user.email ?: ""),
                    "phone" to (user.phoneNumber ?: ""),
                    "displayName" to (user.displayName ?: ""),
                    "expensesCount" to expenses.size,
                    "cardsCount" to cards.size
                )
                batch.set(metaDoc, meta, SetOptions.merge())

                batch.commit().await()

                _syncState.value = SyncState.SYNCED
                val now = System.currentTimeMillis()
                _lastSyncTime.value = now
                _syncMessage.value = "All ${expenses.size} expenses & ${cards.size} cards backed up to Firestore"
                onComplete(true, "Cloud sync completed successfully")
            } catch (e: Exception) {
                Log.e(tag, "Firestore sync error", e)
                _syncState.value = SyncState.ERROR
                _syncMessage.value = "Sync failed: ${e.localizedMessage ?: "Unknown error"}"
                onComplete(false, e.localizedMessage ?: "Sync failed")
            }
        }
    }

    fun syncSingleExpense(expense: ExpenseEntity) {
        val user = auth.currentUser ?: return
        scope.launch {
            try {
                firestore.collection("users")
                    .document(user.uid)
                    .collection("expenses")
                    .document(expense.id.toString())
                    .set(FirestoreExpense.fromEntity(expense), SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                Log.w(tag, "Single expense sync failed: ${e.message}")
            }
        }
    }

    fun deleteExpenseFromCloud(id: Long) {
        val user = auth.currentUser ?: return
        scope.launch {
            try {
                firestore.collection("users")
                    .document(user.uid)
                    .collection("expenses")
                    .document(id.toString())
                    .delete()
                    .await()
            } catch (e: Exception) {
                Log.w(tag, "Single expense cloud deletion failed: ${e.message}")
            }
        }
    }

    fun syncSingleCard(card: CardAccountEntity) {
        val user = auth.currentUser ?: return
        scope.launch {
            try {
                firestore.collection("users")
                    .document(user.uid)
                    .collection("cards")
                    .document(card.id.toString())
                    .set(FirestoreCard.fromEntity(card), SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                Log.w(tag, "Single card sync failed: ${e.message}")
            }
        }
    }

    fun deleteCardFromCloud(id: Long) {
        val user = auth.currentUser ?: return
        scope.launch {
            try {
                firestore.collection("users")
                    .document(user.uid)
                    .collection("cards")
                    .document(id.toString())
                    .delete()
                    .await()
            } catch (e: Exception) {
                Log.w(tag, "Single card cloud deletion failed: ${e.message}")
            }
        }
    }

    fun fetchCloudData(
        onSuccess: (List<ExpenseEntity>, List<CardAccountEntity>) -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onError("User not signed in")
            return
        }

        scope.launch {
            try {
                _syncState.value = SyncState.SYNCING
                val uid = user.uid

                val expSnapshot = firestore.collection("users")
                    .document(uid)
                    .collection("expenses")
                    .get()
                    .await()

                val cardSnapshot = firestore.collection("users")
                    .document(uid)
                    .collection("cards")
                    .get()
                    .await()

                val expenses = expSnapshot.documents.mapNotNull { it.toObject(FirestoreExpense::class.java)?.toEntity() }
                val cards = cardSnapshot.documents.mapNotNull { it.toObject(FirestoreCard::class.java)?.toEntity() }

                _syncState.value = SyncState.SYNCED
                _lastSyncTime.value = System.currentTimeMillis()
                _syncMessage.value = "Pulled ${expenses.size} expenses & ${cards.size} cards from cloud"
                onSuccess(expenses, cards)
            } catch (e: Exception) {
                Log.e(tag, "Fetch cloud data error", e)
                _syncState.value = SyncState.ERROR
                _syncMessage.value = "Failed to fetch cloud data"
                onError(e.localizedMessage ?: "Failed to fetch data from Firestore")
            }
        }
    }

    fun attachCloudListeners(
        onExpensesChanged: (List<ExpenseEntity>) -> Unit,
        onCardsChanged: (List<CardAccountEntity>) -> Unit
    ) {
        val user = auth.currentUser ?: return
        detachCloudListeners()

        val uid = user.uid
        expenseListenerRegistration = firestore.collection("users")
            .document(uid)
            .collection("expenses")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(tag, "Expenses snapshot listener error", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val expenses = snapshot.documents.mapNotNull {
                        it.toObject(FirestoreExpense::class.java)?.toEntity()
                    }
                    onExpensesChanged(expenses)
                }
            }

        cardListenerRegistration = firestore.collection("users")
            .document(uid)
            .collection("cards")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(tag, "Cards snapshot listener error", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val cards = snapshot.documents.mapNotNull {
                        it.toObject(FirestoreCard::class.java)?.toEntity()
                    }
                    onCardsChanged(cards)
                }
            }
    }

    fun detachCloudListeners() {
        expenseListenerRegistration?.remove()
        expenseListenerRegistration = null
        cardListenerRegistration?.remove()
        cardListenerRegistration = null
    }
}
