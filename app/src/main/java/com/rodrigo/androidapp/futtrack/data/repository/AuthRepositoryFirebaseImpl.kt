import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rodrigo.androidapp.futtrack.domain.model.UserProfile
import com.rodrigo.androidapp.futtrack.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryFirebaseImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    private val usersCollection = firestore.collection("users")

    override fun getCurrentUserStream(): Flow<UserProfile?> = callbackFlow {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = usersCollection.document(currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val userProfile = snapshot.toObject(UserProfile::class.java)
                    trySend(userProfile)
                } else {
                    trySend(null)
                }
            }

        awaitClose { listener.remove() }
    }

    override suspend fun syncUserAuth() {
        val currentUser = auth.currentUser ?: return

        val docRef = usersCollection.document(currentUser.uid)
        val snapshot = docRef.get().await()

        if (!snapshot.exists()) {
            val newUser = UserProfile(
                uid = currentUser.uid,
                isAdmin = false
            )
            docRef.set(newUser).await()
        }
    }
}