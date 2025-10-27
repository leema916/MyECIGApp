package com.example.appecig;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appecig.teacherRV.Course;
import com.example.appecig.teacherRV.CourseAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TeacherActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView userNameTextView;
    ProgressDialog progressDialog;
    private Spinner fieldSpinner, subjectSpinner, groupSpinner;
    private String videoUrl = null;
    private String pdfUrl = null;
    private Button selectVideoButton, selectPdfButton, uploadCourseButton;
    private int uploadCounter = 0;

    private EditText courseNameInput, courseNotesInput;
    private ArrayAdapter<String> fieldAdapter, subjectAdapter, groupAdapter;
    private static final int REQUEST_CODE_SELECT_VIDEO = 1;
    private static final int REQUEST_CODE_SELECT_PDF = 2;
    private Uri videoUri, pdfUri;
    private FirebaseFirestore firestore;
    private CourseAdapter courseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.blue));
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        firestore = FirebaseFirestore.getInstance();
        userNameTextView = findViewById(R.id.name);

        RecyclerView courseRecyclerView = findViewById(R.id.rvTeacher);


        fieldSpinner = findViewById(R.id.field_spinner);
        subjectSpinner = findViewById(R.id.subject_spinner);
        groupSpinner = findViewById(R.id.group_spinner);
        selectVideoButton = findViewById(R.id.select_video_button);
        selectPdfButton = findViewById(R.id.select_pdf_button);
        uploadCourseButton = findViewById(R.id.upload_course_button);
        courseNotesInput = findViewById(R.id.course_notes_input);
        courseNameInput = findViewById(R.id.course_name_input);
        selectVideoButton.setOnClickListener(v -> selectFile("video/*"));
        selectPdfButton.setOnClickListener(v -> selectFile("application/pdf"));
        progressDialog = new ProgressDialog(TeacherActivity.this);
        courseRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        fieldAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        fieldAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fieldSpinner.setAdapter(fieldAdapter);
        groupAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(groupAdapter);
        subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectSpinner.setAdapter(subjectAdapter);

        fetchAndPopulateSpinners();
        fetchCurrentUserData();

        uploadCourseButton.setOnClickListener(v -> {

            if (videoUri == null || pdfUri == null) {
                Toast.makeText(this, "Please select a video and a PDF", Toast.LENGTH_SHORT).show();
                return;
            }

            String courseName = courseNameInput.getText().toString().trim();
            String courseNotes = courseNotesInput.getText().toString().trim();
            if (courseName.isEmpty() || courseNotes.isEmpty()) {
                Toast.makeText(this, "Please fill in Course Name and Notes fields", Toast.LENGTH_SHORT).show();
                return;
            }
            progressDialog.setMessage("Uploading Course...");
            progressDialog.show();
            if (uploadCounter == 2) {
                handleUploadCourse();
                resetVariables();
            }

        });
    }

    private void selectFile(String mimeType) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(mimeType);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (mimeType.startsWith("video/")) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO);
        } else if (mimeType.startsWith("application/pdf")) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_PDF);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_SELECT_VIDEO) {
                videoUri = data.getData();
                handleVideoUpload(videoUri);
                progressDialog.setMessage("Uploading video...");
                progressDialog.show();
            } else if (requestCode == REQUEST_CODE_SELECT_PDF) {
                pdfUri = data.getData();
                handlePdfUpload(pdfUri);
                progressDialog.setMessage("Uploading Pdf...");
                progressDialog.show();
            }
        }
    }
    private void handleVideoUpload(Uri fileUri) {
        if (fileUri != null) {

            final StorageReference videoRef = FirebaseStorage.getInstance().getReference()
                    .child("course_videos/" + UUID.randomUUID().toString());

            UploadTask uploadTask = videoRef.putFile(fileUri);

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                videoRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                    videoUrl = downloadUrl.toString();
                    uploadCounter++;
                    progressDialog.dismiss();

                });

            }).addOnFailureListener(e -> {
                handleError();
                Toast.makeText(this, "Video Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void handlePdfUpload(Uri fileUri) {
        if (fileUri != null) {

            final StorageReference videoRef = FirebaseStorage.getInstance().getReference()
                    .child("course_pdfs/" + UUID.randomUUID().toString());

            UploadTask uploadTask = videoRef.putFile(fileUri);

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                videoRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                    pdfUrl = downloadUrl.toString();
                    uploadCounter++;
                    progressDialog.dismiss();


                });

            }).addOnFailureListener(e -> {
                handleError();
                Toast.makeText(this, "Pdf Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }


    private void resetVariables() {
        videoUrl = null;
        pdfUrl = null;
        uploadCounter = 0;
        courseNameInput.setText("");
        courseNotesInput.setText("");
    }

    private void handleError() {
        resetVariables();
    }
    private void handleUploadCourse() {
        String courseName = courseNameInput.getText().toString();
        String courseNotes = courseNotesInput.getText().toString();
        String field = fieldSpinner.getSelectedItem().toString();
        String subject = subjectSpinner.getSelectedItem().toString();
        String group = groupSpinner.getSelectedItem().toString();
        String teacherName = userNameTextView.getText().toString();

        Course newCourse = new Course();
        newCourse.setName(courseName);
        newCourse.setNotes(courseNotes);
        newCourse.setField(field);
        newCourse.setSubject(subject);
        newCourse.setGroup(group);

        newCourse.setVideoUrl(videoUrl);
        newCourse.setPdfUrl(pdfUrl);
        newCourse.setTeacherName(teacherName);

        firestore.collection("courses")
                .add(newCourse)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Course Created!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    resetVariables();
                    fetchTeacherCourses();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Course Creation Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void fetchAndPopulateSpinners() {
        firestore.collection("fields").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        fieldAdapter.clear();
                        for (DocumentSnapshot doc : task.getResult()) {
                            String fieldName = doc.getString("name");
                            if(fieldName != null) {
                                fieldAdapter.add(fieldName);
                            }
                        }
                        fieldAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(TeacherActivity.this, "Error fetching fields: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        firestore.collection("subjects").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        subjectAdapter.clear();
                        for (DocumentSnapshot doc : task.getResult()) {
                            String fieldName = doc.getString("name");
                            if(fieldName != null) {
                                subjectAdapter.add(fieldName);
                            }
                        }
                        subjectAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(TeacherActivity.this, "Error fetching subjects: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        firestore.collection("groups").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        groupAdapter.clear();
                        for (DocumentSnapshot doc : task.getResult()) {
                            String fieldName = doc.getString("name");
                            if(fieldName != null) {
                                groupAdapter.add(fieldName);
                            }
                        }
                        groupAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(TeacherActivity.this, "Error fetching groups: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void fetchTeacherCourses() {
        String currentTeacherName = userNameTextView.getText().toString();
        firestore.collection("courses")
                .whereEqualTo("teacherName", currentTeacherName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Course> courses = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            Course course = doc.toObject(Course.class);
                            courses.add(course);
                        }
                        RecyclerView courseRecyclerView = findViewById(R.id.rvTeacher);
                        courseAdapter = new CourseAdapter(this, courses);
                        courseRecyclerView.setAdapter(courseAdapter);

                    } else {
                        Toast.makeText(TeacherActivity.this, "Error fetching courses: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void fetchCurrentUserData() {
        String currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        DocumentReference userRef = db.collection("users").document(currentUserId);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()) {
                    String name = doc.getString("name");
                    userNameTextView.setText(name);
                    fetchTeacherCourses();
                } else {
                    Log.d("TAG", "No user document found!");
                }
            } else {
                Log.w("TAG", "Error fetching user data:", task.getException());
            }
        });
    }



    private void logout() {
        mAuth.signOut();
        startActivity(new Intent(this, Login.class));
        finish();
    }
}
