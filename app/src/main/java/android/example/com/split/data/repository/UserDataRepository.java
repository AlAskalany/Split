package android.example.com.split.data.repository;

import android.example.com.split.data.entity.User;
import android.support.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class UserDataRepository {

  private static final String TAG = "UserDataRepository";
  public OnUserId listener;
  private FirebaseFirestore db;

  // create new Auth user
  public void createNewUser(User user, String userAuthId, final OnUserCreated listener) {
    db = FirebaseFirestore.getInstance();
    db.collection("users").document(userAuthId).set(user)
      .addOnSuccessListener(new OnSuccessListener<Void>() {
        @Override
        public void onSuccess(Void aVoid) {
          listener.onUserCreated(true);
        }
      }).addOnFailureListener(new OnFailureListener() {
      @Override
      public void onFailure(@NonNull Exception e) {
        listener.onUserCreated(false);
      }
    });

  }

  // create new user and add it to Auth user's list
  public void addNewContact(final User user, final String userAuthId, final OnContactCreated
      listener) {
    db = FirebaseFirestore.getInstance();
    db.collection("users").add(user)
      .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
        @Override
        public void onSuccess(DocumentReference documentReference) {
          final String userId = documentReference.getId();
          db.collection("users").document(userAuthId).get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
              @Override
              public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                  DocumentSnapshot snapshot = task.getResult();
                  User myUser = snapshot.toObject(User.class);
                  myUser.addToContactList(userId);
                  db.collection("users").document(userAuthId).set(myUser)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                      @Override
                      public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                          listener.onContactCreated(true);
                        } else {
                          listener.onContactCreated(false);
                        }
                      }
                    });

                }
              }
            });
        }


      }).addOnFailureListener(new OnFailureListener() {
      @Override
      public void onFailure(@NonNull Exception e) {
        listener.onContactCreated(false);

      }
    });

  }


  // get the document is of the current auth user
  public void getDocumentId(String user_auth_id, final OnUserId listener) {
    db = FirebaseFirestore.getInstance();
    final String[] data = {""};
    db.collection("users").limit(1).whereEqualTo("id", user_auth_id).get()

      .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
        @Override
        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {


          for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {

            data[0] = documentSnapshot.getId();
          }
          listener.onUserId(data[0]);
        }

      });
  }
          /*.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
              @Override
              public void onComplete(@NonNull Task<QuerySnapshot> task) {
                  if (task.isSuccessful()) {
                      DocumentSnapshot asd = task.getResult().getDocuments().get(0);
                      id[0] = asd.getId();


                *//* User user = asd.toObject(User.class);

                   for(QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                     Log.d(TAG, "onComplete: " + documentSnapshot.getId());

                 }*//*
                  }

              }
          });

        return id[0];*/


  public void isUserExist(String id, final IsUserExist listener) {
    db = FirebaseFirestore.getInstance();
    db.collection("users").document(id).get()
      .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
        // if got document or connected to collection ref but document not exists
        @Override
        public void onSuccess(DocumentSnapshot documentSnapshot) {
          if (documentSnapshot.exists())
            listener.isUserExist(true);
          else
            listener.isUserExist(false);
        }
      })
      // if fail to connect or get collection ref
      .addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
          listener.isUserExist(false);
        }
      });
  }

  // create new user and add it to Auth user's list
  public void addNewContact1(final User user, final String userAuthId, final OnContactCreated
      listener) {
    db = FirebaseFirestore.getInstance();
    db.collection("users").add(user)
      .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
        @Override
        public void onSuccess(DocumentReference documentReference) {
          final String userId = documentReference.getId();
          db.collection("users").document(userAuthId).
              update("contacts", userId).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
              listener.onContactCreated(true);
            }
          }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
              listener.onContactCreated(false);
            }
          });
        }

      });
  }

  // get list of user's contact

  public void getContactlist(final String userAuthId, final OnGetContact listener) {
    db = FirebaseFirestore.getInstance();
    db.collection("users").document(userAuthId).get()
      .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
        @Override
        public void onSuccess(DocumentSnapshot documentSnapshot) {
          final List<User> contactUserList = new ArrayList<>();
          if (documentSnapshot.exists()) {
            User user = documentSnapshot.toObject(User.class);
            // list of users document id
            List<String> contactList = user.getContacts();
            for (int i = 0; i < contactList.size() - 1; i++) {
              db.collection("users").document(contactList.get(i)).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                  @Override
                  public void onSuccess(DocumentSnapshot documentSnapshot) {
                    // list of contact usersList<User> contactUserList;

                    User contactUser = documentSnapshot.toObject(User.class);
                    contactUserList.add(contactUser);
                  }
                });
            }

            listener.onGetContact(contactUserList);
          }

        }
      });
  }

  // get user details

  public void getUserDetail(final String userAuthId, final OnUserDetails listener) {
    db = FirebaseFirestore.getInstance();
    db.collection("users").document(userAuthId).get()
      .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
        @Override
        public void onSuccess(DocumentSnapshot documentSnapshot) {
          if (documentSnapshot.exists()) {
            User user = documentSnapshot.toObject(User.class);
            listener.onUserDetails(user);
          }

        }
      }).addOnFailureListener(new OnFailureListener() {
      @Override
      public void onFailure(@NonNull Exception e) {

      }
    });
  }

  public interface OnUserId {

    void onUserId(String userId);


  }

  public interface OnUserCreated {

    void onUserCreated(Boolean userCreated);
  }

  public interface OnContactCreated {

    void onContactCreated(Boolean contactCreated);
  }

  public interface OnGetContact {

    void onGetContact(List<User> contactUser);
  }

  public interface IsUserExist {

    void isUserExist(Boolean userExist);
  }

  public interface OnUserDetails {

    void onUserDetails(User user);
  }

}
