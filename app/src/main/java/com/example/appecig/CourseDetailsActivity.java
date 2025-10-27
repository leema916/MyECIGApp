package com.example.appecig;

import android.app.DownloadManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appecig.teacherRV.Course;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.firebase.firestore.FirebaseFirestore;

public class CourseDetailsActivity extends AppCompatActivity {

    private PlayerView videoPlayer;
    private Button downloadPdfButton, backButton;
    private SimpleExoPlayer exoPlayer;
    private TextView courseName;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_details);
        downloadPdfButton = findViewById(R.id.download_pdf_button);
        backButton = findViewById(R.id.back_button);
        videoPlayer = findViewById(R.id.video_player);
        courseName = findViewById(R.id.courseName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.blue));
        }
        db = FirebaseFirestore.getInstance();


        Intent intent = getIntent();
        Course course = (Course) intent.getSerializableExtra("course");
        courseName.setText(course.getName());


        assert course != null;
        initializeVideoPlayer(course.getVideoUrl());

        downloadPdfButton.setOnClickListener(v -> {
            downloadPdf(course.getPdfUrl());
        });

        backButton.setOnClickListener(v -> {
            finish();
        });
    }

    private void downloadPdf(String pdfUrl) {
        String fileName = "downloaded_course_pdf.pdf";
        String dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(pdfUrl))
                .setTitle(fileName)
                .setDescription("Downloading PDF...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true);

        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadManager.enqueue(request);
        }
    }

    private void initializeVideoPlayer(String videoUrl) {
        exoPlayer = new SimpleExoPlayer.Builder(this).build();
        videoPlayer.setPlayer(exoPlayer);

        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.play();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.release();
        }
    }


    }