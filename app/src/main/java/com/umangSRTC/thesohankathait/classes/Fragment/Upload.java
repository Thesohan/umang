package com.umangSRTC.thesohankathait.classes.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;
import com.umangSRTC.thesohankathait.classes.Activity.Features;
import com.umangSRTC.thesohankathait.classes.Activity.Functionality;
import com.umangSRTC.thesohankathait.classes.Utill.CompressImages;
import com.umangSRTC.thesohankathait.classes.Utill.Initialisation;
import com.umangSRTC.thesohankathait.classes.model.Notices;
import com.umangSRTC.thesohankathait.classes.Utill.Admin;
import com.umangSRTC.thesohankathait.umang.R;

import java.io.IOException;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class Upload extends Fragment {

    private ImageView imageView;
    private EditText titleEditText,descriptionEditText,linkEditText=null;
    private Button uploadButton;
    private Spinner spinner;
    private Context context;
    private Uri imageUri=null;
    private ProgressDialog progressDialog;
    private String imageURl;
    private String selectedSchool=null;
    private ArrayAdapter<String>  spinnerArrayAdapter;
    private TextView aboutUmangTextview;
//    private ImageButton cameraImageButton;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.upload_fragment,container,false);

        context=getContext();
//        cameraImageButton=view.findViewById(R.id.cameraImageButton);
        imageView=view.findViewById(R.id.uploadImageView);
        titleEditText=view.findViewById(R.id.titleEditText);
        descriptionEditText=view.findViewById(R.id.descriptionEditText);
        uploadButton=view.findViewById(R.id.uploadButton);
        spinner=view.findViewById(R.id.spinner);
        linkEditText=view.findViewById(R.id.linkEditText);

        aboutUmangTextview=view.findViewById(R.id.aboutUmangTextView);
        aboutUmangTextview.setVisibility(View.VISIBLE);

        aboutUmangTextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent requestIntent = new Intent(getContext(), Features.class);
                requestIntent.putExtra("FRAGMENT_NAME","AboutUmang");
                startActivity(requestIntent);
            }
        });


       // Collections.sort(Initialisation.schools.subList(1,Initialisation.schools.size()));
        spinnerArrayAdapter = new ArrayAdapter<String>
                (context, android.R.layout.simple_spinner_item,
                        Initialisation.schools); //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImage();
            }
        });



        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(allSet()){
                    selectedSchool=spinner.getSelectedItem().toString();
                    showProgressDialog();
                    if(imageUri!=null)
                        uploadImage(imageUri);
                    else{
                        uploadintoFirebase();
                    }
                }
                else{
                    Toast.makeText(context, "Please fill all fields first!", Toast.LENGTH_LONG).show();
                }
            }
        });

//        cameraImageButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//                }
//            }
//        });

        return view;
    }

    private void getImage() {
        Intent chooseAndSetImageIntent=new Intent(Intent.ACTION_GET_CONTENT);
        chooseAndSetImageIntent.setType("image/*");
        startActivityForResult(chooseAndSetImageIntent,1);
    }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if(requestCode==1&&resultCode==getActivity().RESULT_OK&&data!=null){
                imageUri=data.getData();
                Glide.with(context).load(imageUri).into(imageView);
            }
        }

    private boolean allSet() {
        if(!spinner.getSelectedItem().toString().equals("Select Schools") &&
                !titleEditText.getText().toString().trim().equals("") &&
                !descriptionEditText.getText().toString().trim().equals(""))
            return true;
        else
            return false;

    }
    private void showProgressDialog() {
        progressDialog =new ProgressDialog(getContext());
        progressDialog.setMessage("Please wait...");
        progressDialog.setProgressStyle(R.style.Animation_Design_BottomSheetDialog);
        progressDialog.setProgress(0);
        progressDialog.setCancelable(false);
        progressDialog.show();


    }
    private void uploadImage(Uri imageUri) {
        UUID uuid=UUID.randomUUID();
        CompressImages compressImages=new CompressImages(context,imageUri);
        try {
            byte[] data=compressImages.compress();
            FirebaseStorage.getInstance().getReference().child(uuid.toString()).putBytes(data).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.setMessage("please wait..."+((100*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount())+"%");

                }
            }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        task.getResult().getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if(task.isSuccessful()) {
                                    Toast.makeText(getContext(), "file uploaded", Toast.LENGTH_SHORT).show();
                                    imageURl =task.getResult().toString();
                                    uploadintoFirebase();

                                }
                                else{
                                    progressDialog.dismiss();
                                    Toast.makeText(getContext(), ""+task.getException().toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    else{
                        Toast.makeText(getContext(), ""+task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (IOException e) {
            Toast.makeText(getContext(), ""+e.toString(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    private void uploadintoFirebase() {
        progressDialog.dismiss();
        Notices notices=new Notices(descriptionEditText.getText().toString(),titleEditText.getText().toString(),FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),imageURl);

        //if user enter any link than set that link into notice
        if(!linkEditText.getText().toString().trim().equals("")){
            notices.setLink(linkEditText.getText().toString());
        }

        if(Admin.CheckAdmin(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
            FirebaseDatabase.getInstance().getReference("Category").child(selectedSchool).push().setValue(notices).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(getContext(), "notice send", Toast.LENGTH_SHORT).show();

                    //IF we call this method outside than it can be executed before the data got uploaded.
                    goToFirstPage();
                }
            });
        }
        else{
            FirebaseDatabase.getInstance().getReference("Requests").child(selectedSchool).push().setValue(notices).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(getContext(), "Your NoticeRequest is send to admin, thankyou!", Toast.LENGTH_SHORT).show();
                    goToFirstPage();
                }
            });
        }

    }

    private void goToFirstPage() {

    Intent intent=new Intent(context,Functionality.class);
    startActivity(intent);
    getActivity().finish();
    }


    public static Upload newInstance() {
        
        Bundle args = new Bundle();
        
        Upload fragment = new Upload();
        fragment.setArguments(args);
        return fragment;
    }
//
//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//        if (isVisibleToUser && isResumed()) {
//
//            spinnerArrayAdapter.clear();
//
//           SpinnerAdapter spinnerArrayAdapter = new ArrayAdapter<String>
//                    (context, android.R.layout.simple_spinner_item,
//                            Initialisation.schools); //selected item will look like a spinner set from XML
//            ((ArrayAdapter) spinnerArrayAdapter).setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            spinner.setAdapter(spinnerArrayAdapter);
//            //do your Stuffs
//        }
//    }
}
