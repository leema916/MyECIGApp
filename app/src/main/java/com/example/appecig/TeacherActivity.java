package com.example.appecig;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TeacherActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SELECT_VIDEO = 1;
    private static final int REQUEST_CODE_SELECT_PDF = 2;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    private TextView userNameTextView;
    private EditText courseNameInput, courseNotesInput;
    private Spinner fieldSpinner, subjectSpinner, groupSpinner;
    private Button selectVideoButton, selectPdfButton, uploadCourseButton;

    private Uri videoUri, pdfUri;
    private String videoUrl, pdfUrl;
    private int uploadCounter = 0;

    private ProgressDialog progressDialog;
    private CourseAdapter courseAdapter;

    private ArrayAdapter<String> fieldAdapter, subjectAdapter, groupAdapter;

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
        firestore = FirebaseFirestore.getInstance();

        userNameTextView = findViewById(R.id.name);
        courseNameInput = findViewById(R.id.course_name_input);
        courseNotesInput = findViewById(R.id.course_notes_input);

        fieldSpinner = findViewById(R.id.field_spinner);
        subjectSpinner = findViewById(R.id.subject_spinner);
        groupSpinner = findViewById(R.id.group_spinner);

        selectVideoButton = findViewById(R.id.select_video_button);
        selectPdfButton = findViewById(R.id.select_pdf_button);
        uploadCourseButton = findViewById(R.id.upload_course_button);

        RecyclerView recyclerView = findViewById(R.id.rvTeacher);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        fieldAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        fieldAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fieldSpinner.setAdapter(fieldAdapter);

        subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectSpinner.setAdapter(subjectAdapter);

        groupAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(groupAdapter);

        selectVideoButton.setOnClickListener(v -> selectFile("video/*"));
        selectPdfButton.setOnClickListener(v -> selectFile("application/pdf"));

        uploadCourseButton.setOnClickListener(v -> startUpload());

        fetchAndPopulateSpinners();
        fetchCurrentUserData();
    }

    // ---------------- FILE PICKER ----------------

    private void selectFile(String mimeType) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType(mimeType);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        if (mimeType.startsWith("video/")) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO);
        } else {
            startActivityForResult(intent, REQUEST_CODE_SELECT_PDF);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {

            Uri uri = data.getData();

            // 🔥 THIS IS THE FIX
            getContentResolver().takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            if (requestCode == REQUEST_CODE_SELECT_VIDEO) {
                videoUri = uri;
                Toast.makeText(this, "Video selected", Toast.LENGTH_SHORT).show();
            }
            else if (requestCode == REQUEST_CODE_SELECT_PDF) {
                pdfUri = uri;
                Toast.makeText(this, "PDF selected", Toast.LENGTH_SHORT).show();
            }
        }
    }


    // ---------------- UPLOAD FLOW ----------------

    private void startUpload() {
        if (videoUri == null || pdfUri == null) {
            Toast.makeText(this, "Select both video and PDF", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = courseNameInput.getText().toString().trim();
        String notes = courseNotesInput.getText().toString().trim();

        if (name.isEmpty() || notes.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadCounter = 0;
        progressDialog.setMessage("Uploading course...");
        progressDialog.show();

        uploadVideo();
        uploadPdf();
    }

    private void uploadVideo() {
        StorageReference ref = FirebaseStorage.getInstance()
                .getReference("course_videos/" + UUID.randomUUID());

        ref.putFile(videoUri)
                .addOnSuccessListener(task ->
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            videoUrl = uri.toString();
                            checkUploadFinished();
                        }))
                .addOnFailureListener(e -> showError(e.getMessage()));
    }

    private void uploadPdf() {
        StorageReference ref = FirebaseStorage.getInstance()
                .getReference("course_pdfs/" + UUID.randomUUID());

        ref.putFile(pdfUri)
                .addOnSuccessListener(task ->
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            pdfUrl = uri.toString();
                            checkUploadFinished();
                        }))
                .addOnFailureListener(e -> showError(e.getMessage()));
    }

    private void checkUploadFinished() {
        uploadCounter++;
        if (uploadCounter == 2) {
            createCourse();
        }
    }

    private void showError(String msg) {
        progressDialog.dismiss();
        Toast.makeText(this, "Upload failed: " + msg, Toast.LENGTH_LONG).show();
    }

    // ---------------- FIRESTORE ----------------

    private void createCourse() {
        Course course = new Course();
        course.setName(courseNameInput.getText().toString());
        course.setNotes(courseNotesInput.getText().toString());
        course.setField(fieldSpinner.getSelectedItem().toString());
        course.setSubject(subjectSpinner.getSelectedItem().toString());
        course.setGroup(groupSpinner.getSelectedItem().toString());
        course.setTeacherName(userNameTextView.getText().toString());
        course.setVideoUrl(videoUrl);
        course.setPdfUrl(pdfUrl);

        firestore.collection("courses")
                .add(course)
                .addOnSuccessListener(doc -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Course uploaded!", Toast.LENGTH_SHORT).show();
                    reset();
                    fetchTeacherCourses();
                })
                .addOnFailureListener(e -> showError(e.getMessage()));
    }

    private void reset() {
        videoUri = null;
        pdfUri = null;
        videoUrl = null;
        pdfUrl = null;
        courseNameInput.setText("");
        courseNotesInput.setText("");
    }

    // ---------------- DATA LOADING ----------------

    private void fetchAndPopulateSpinners() {
        loadSpinner("fields", fieldAdapter);
        loadSpinner("subjects", subjectAdapter);
        loadSpinner("groups", groupAdapter);
    }

    private void loadSpinner(String collection, ArrayAdapter<String> adapter) {
        firestore.collection(collection).get().addOnSuccessListener(result -> {
            adapter.clear();
            for (DocumentSnapshot doc : result) {
                String name = doc.getString("name");
                if (name != null) adapter.add(name);
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void fetchTeacherCourses() {
        firestore.collection("courses")
                .whereEqualTo("teacherName", userNameTextView.getText().toString())
                .get()
                .addOnSuccessListener(result -> {
                    List<Course> list = new ArrayList<>();
                    for (DocumentSnapshot doc : result) {
                        list.add(doc.toObject(Course.class));
                    }
                    RecyclerView rv = findViewById(R.id.rvTeacher);
                    courseAdapter = new CourseAdapter(this, list);
                    rv.setAdapter(courseAdapter);
                });
    }

    private void fetchCurrentUserData() {
        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        firestore.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        userNameTextView.setText(doc.getString("name"));
                        fetchTeacherCourses();
                    }
                });
    }
}
