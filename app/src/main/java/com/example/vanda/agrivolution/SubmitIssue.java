package com.example.vanda.agrivolution;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class SubmitIssue extends AppCompatActivity
{
    Button btnSubmitIssue;
    Button btnCancelTicket;
    ImageButton ticketImgBtn;
    EditText description;
    EditText farmName;
    EditText farmAdd;
    EditText farmArea;
    EditText farmState;
    EditText farmZip;
    EditText ticketTitle;
    EditText optionalContact;
    EditText date;
    EditText email;
    private StorageReference mStorage;
    private Uri ticketURL = null;
    private ProgressDialog tDialog;
    private static final int CAMERA_REQUEST = 1888;
    private ImageView imgUpload;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private String tFarmName;
    private String tFarmAdd;
    private String tfarmArea;
    private String tfarmState;
    private String tfarmPin;
    private String tDate;
    private String temail;
    private String tContact;
    private String tDesc;
    private String tTicketTitle;
    private FirebaseAuth firebaseauthObj;
    private DatabaseReference mDatabase;
    private DatabaseReference BlogDatabase;
    private static final int GALLERY_REQUEST =1;
    private String UserId;
    private String tname;
    private String ticketStatus = "Open";
    @TargetApi(23)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_ticket);
        setupUIViews();

        firebaseauthObj = FirebaseAuth.getInstance();
        UserId = firebaseauthObj.getUid();
        mStorage = FirebaseStorage.getInstance().getReference();
        BlogDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("User").child(UserId);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String fname = dataSnapshot.child("firstName").getValue().toString();
                String lname = dataSnapshot.child("lastName").getValue().toString();
                tname = fname.concat(" ").concat(lname);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Tickets").child(UserId);
        tDialog = new ProgressDialog(this);

        ticketImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_REQUEST);
            }
        });
        btnSubmitIssue.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(validate())
                {
                    tDialog.setMessage("Sending new Ticket");
                    createTicket();
                    Toast.makeText(SubmitIssue.this, "Issue Submitted!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SubmitIssue.this, Dashboard.class));
                }
                else
                {
                    Toast.makeText(SubmitIssue.this,"Please Enter all the details !",Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnCancelTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(SubmitIssue.this, "Ticket Cancelled!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SubmitIssue.this, Dashboard.class));
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera Permission Granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new
                        Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_LONG).show();
            }

        }
    }
    /*protected void onActivityResult(int requestCode ,int resultCode, Intent data)
    {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imgUpload.setImageBitmap(photo);
        }
    }*/
    public void createTicket(){

            tDialog.show();
            StorageReference filepath = mStorage.child("Ticket_Images").child(ticketURL.getLastPathSegment());
            filepath.putFile(ticketURL).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Task<Uri> downloadUrlTask = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    downloadUrlTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUri = uri;
                            String imageUrl = downloadUri.toString();
                            DatabaseReference newTicket = mDatabase.push();
                            String pushedKey = newTicket.getKey();
                            Ticket ticket = new Ticket(tFarmName,tFarmAdd,tfarmArea,tfarmState,tfarmPin,tTicketTitle,tDate,temail,tContact,tDesc, imageUrl, ticketStatus, tname,UserId);
                            newTicket.setValue(ticket);
                            DatabaseReference newBlog = BlogDatabase.child(pushedKey);
                            newBlog.setValue(ticket);
                            tDialog.dismiss();
                            Toast.makeText(SubmitIssue.this,"New Ticket Submitted!",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SubmitIssue.this,Dashboard.class));
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(SubmitIssue.this, "Ticket Failed", Toast.LENGTH_SHORT).show();
                }
            });
    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){
            ticketURL = data.getData();
            CropImage.activity(ticketURL)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                Uri resultUri = result.getUri();
                ticketImgBtn.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
    // TODO validate must be implemented, low Priority, will complete later. For now it will auto accept.
    private void setupUIViews()
    {
        btnSubmitIssue = findViewById(R.id.btbSubmitTicket);
        btnCancelTicket = findViewById(R.id.btnCancel);
        imgUpload = findViewById(R.id.ImgUpload);
        ticketImgBtn = findViewById(R.id.imageSelect_ticket);
        farmName = findViewById(R.id.farmName);
        farmAdd = findViewById(R.id.farmAddress);
        farmArea= findViewById(R.id.farmArea);
        farmState= findViewById(R.id.farmState);
        farmZip = findViewById(R.id.farmPin);
        ticketTitle = findViewById(R.id.ticketTitle);
        date = findViewById(R.id.date);
        email = findViewById(R.id.email);
        optionalContact = findViewById(R.id.OptionalContact);
        description = findViewById(R.id.description);
    }
    private boolean validate()
    {
        tFarmName = farmName.getText().toString();
        tFarmAdd = farmAdd.getText().toString();
        tfarmArea= farmArea.getText().toString();
        tfarmState = farmState.getText().toString();
        tfarmPin = farmZip.getText().toString();
        tTicketTitle = ticketTitle.getText().toString();
        tDate = date.getText().toString();
        temail = email.getText().toString();
        tContact = optionalContact.getText().toString();
        tDesc = description.getText().toString();


        if (tFarmName.isEmpty() || tFarmAdd.isEmpty() || tTicketTitle.isEmpty() || tDate.isEmpty() || tDesc.isEmpty() || tfarmPin.isEmpty()) {
            return false;
        }else{
            return true;
        }

    }
}