package mo.zain.notes.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.textfield.TextInputEditText;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import mo.zain.notes.R;
import mo.zain.notes.database.NotesDatabase;
import mo.zain.notes.entities.Note;
import mo.zain.notes.notification.AlertReceiver;
import mo.zain.notes.notification.TimePickerFragment;

public class CreateNote extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener{

    ImageView imageBack,imageSave;
    private EditText inputNotesTitle,inputNotesSubtitle,inputNotesText;
    TextView textDateTime,textWeb;
    LinearLayout layoutWebURL;
    private View viewSubtitleIndicator;
    private String selectedNoteColor;
    private ImageView ImageNote;
    private String selectedImagePath;
    private static final short REQUEST_CODE_STORAGE_PERMISSION=1;
    private static final short REQUEST_CODE_SELECT_IMAGE=2;

    private AlertDialog dialogAddURL;
    private AlertDialog dialogDeleteNote;

    private Note alreadyAvailableNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        ImageView buttonTimePicker = findViewById(R.id.remember);
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
//        int year = c.get(Calendar.YEAR);
//        c.set(Calendar.YEAR, year + 1);
        buttonTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                DialogFragment timePicker = new TimePickerFragment();
//                timePicker.show(getSupportFragmentManager(), "time picker");
                if (inputNotesTitle.getText().toString().trim().isEmpty()){
                    inputNotesTitle.setError("Note title Can't be empty!");
                    return;
                }else {
                    Intent i = new Intent(AlarmClock.ACTION_SET_ALARM);
                    i.putExtra(AlarmClock.EXTRA_MESSAGE, inputNotesTitle.getText().toString());
                    i.putExtra(AlarmClock.EXTRA_HOUR, (hour + 8760));
                    i.putExtra(AlarmClock.EXTRA_MINUTES, minute);
//                i.putExtra(AlarmClock.EXTRA_DAYS,year);
                    startActivity(i);
                }
            }
        });

        imageBack=findViewById(R.id.imageBack);
        selectedNoteColor="#333333";
        selectedImagePath="";
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        ImageNote=findViewById(R.id.imageNote);
        inputNotesTitle=findViewById(R.id.inputNoteTitle);
        inputNotesSubtitle=findViewById(R.id.inputNoteSubtitle);
        inputNotesText=findViewById(R.id.inputNote);
        viewSubtitleIndicator=findViewById(R.id.viewSubtitleIndicator);
        textWeb=findViewById(R.id.textWebURL);
        layoutWebURL=findViewById(R.id.layoutWebURL);
        textDateTime=findViewById(R.id.txtDateTime);
        textDateTime.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy hh:mm a", Locale.getDefault())
                        .format(new Date())
        );

        imageSave=findViewById(R.id.imageSave);
        imageSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });

        if (getIntent().getBooleanExtra("isViewOrUpdate",false)){
            alreadyAvailableNote=(Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }
        findViewById(R.id.imageRemoveWebURl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textWeb.setText(null);
                layoutWebURL.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.imageRemoveImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageNote.setImageBitmap(null);
                ImageNote.setVisibility(View.GONE);
                findViewById(R.id.imageRemoveImage).setVisibility(View.GONE);
                selectedImagePath ="";
            }
        });

        initMiscellaneous();
        setSubTitleIndicatorColor();
    }

    //here
    private void setViewOrUpdateNote(){
        inputNotesTitle.setText(alreadyAvailableNote.getTitle());
        inputNotesSubtitle.setText(alreadyAvailableNote.getSubTitle());
        inputNotesText.setText(alreadyAvailableNote.getNoteText());
        textDateTime.setText(alreadyAvailableNote.getDateTime());

        if (alreadyAvailableNote.getImagePath()!=null && !alreadyAvailableNote.getImagePath().trim().isEmpty()){
            ImageNote.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableNote.getImagePath()));
            ImageNote.setVisibility(View.VISIBLE);
            findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);
            selectedImagePath=alreadyAvailableNote.getImagePath();
        }
        if (alreadyAvailableNote.getWebLink()!=null && !alreadyAvailableNote.getWebLink().trim().isEmpty()){
            textWeb.setText(alreadyAvailableNote.getWebLink());
            layoutWebURL.setVisibility(View.VISIBLE);
        }

    }

    private void saveNote(){
        if(inputNotesTitle.getText().toString().trim().isEmpty()){
            inputNotesTitle.setError("Note title Can't be empty!");
            return;
        }else if (inputNotesText.getText().toString().trim().isEmpty()){
            inputNotesText.setError("Note Can't be empty!");
            return;
        }
        Note note=new Note();
        note.setTitle(inputNotesTitle.getText().toString().trim());
        note.setSubTitle(inputNotesSubtitle.getText().toString().trim());
        note.setNoteText(inputNotesText.getText().toString().trim());
        note.setDateTime(textDateTime.getText().toString().trim());
        note.setColor(selectedNoteColor);
        note.setImagePath(selectedImagePath);
        if (layoutWebURL.getVisibility()==View.VISIBLE){
            note.setWebLink(textWeb.getText().toString());
        }
        if (alreadyAvailableNote!=null)
        {
            note.setId(alreadyAvailableNote.getId());
        }


        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void,Void,Void>
        {
            @Override
            protected Void doInBackground(Void... voids) {
                NotesDatabase.getDatabase(getApplicationContext()).noteDao().insertNote(note);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
//                Intent intent=new Intent();
//                setResult(RESULT_OK,intent);
//                intent.notify();
                startActivity(new Intent(CreateNote.this,MainActivity.class));
                finish();
//                onBackPressed();

            }
        }

        new SaveNoteTask().execute();
        finish();
    }

    private  void initMiscellaneous(){
        final LinearLayout layoutMiscellaneous=findViewById(R.id.layoutMiscellaneous);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior=BottomSheetBehavior.from(layoutMiscellaneous);
        layoutMiscellaneous.findViewById(R.id.textMiscellaneous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState()!=BottomSheetBehavior.STATE_EXPANDED)
                {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });
        final ImageView imageColor1=layoutMiscellaneous.findViewById(R.id.imageColor1);
        final ImageView imageColor2=layoutMiscellaneous.findViewById(R.id.imageColor2);
        final ImageView imageColor3=layoutMiscellaneous.findViewById(R.id.imageColor3);
        final ImageView imageColor4=layoutMiscellaneous.findViewById(R.id.imageColor4);
        final ImageView imageColor5=layoutMiscellaneous.findViewById(R.id.imageColor5);

        layoutMiscellaneous.findViewById(R.id.viewColor1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor="#333333";
                imageColor1.setImageResource(R.drawable.ic_done);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubTitleIndicatorColor();
            }
        });
        layoutMiscellaneous.findViewById(R.id.viewColor2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor="#FDBE38";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(R.drawable.ic_done);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubTitleIndicatorColor();
            }
        });
        layoutMiscellaneous.findViewById(R.id.viewColor3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor="#FF4842";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(R.drawable.ic_done);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubTitleIndicatorColor();
            }
        });
        layoutMiscellaneous.findViewById(R.id.viewColor4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor="#3A52FC";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(R.drawable.ic_done);
                imageColor5.setImageResource(0);
                setSubTitleIndicatorColor();
            }
        });
        layoutMiscellaneous.findViewById(R.id.viewColor5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor="#000000";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(R.drawable.ic_done);
                setSubTitleIndicatorColor();
            }
        });

        if (alreadyAvailableNote !=null && alreadyAvailableNote.getColor() !=null && !alreadyAvailableNote.getColor().trim().isEmpty()){
            switch (alreadyAvailableNote.getColor()){
                case "#FDBE38":
                    layoutMiscellaneous.findViewById(R.id.viewColor2).performClick();
                    break;
                case "#FF4842":
                    layoutMiscellaneous.findViewById(R.id.viewColor3).performClick();
                    break;
                case "#3A52FC":
                    layoutMiscellaneous.findViewById(R.id.viewColor4).performClick();
                    break;
                case "#000000":
                    layoutMiscellaneous.findViewById(R.id.viewColor5).performClick();
                    break;
            }
        }

      layoutMiscellaneous.findViewById(R.id.layoutAddImage).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            if(ContextCompat.checkSelfPermission(
              getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
            )!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(
                        CreateNote.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_PERMISSION
                );
            }else
            {
                selectImage();
            }
          }
      });
      layoutMiscellaneous.findViewById(R.id.layoutAddLink).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
              showUrlDialog();
          }
      });
      if (alreadyAvailableNote!=null){
          layoutMiscellaneous.findViewById(R.id.layoutDeleteNote).setVisibility(View.VISIBLE);
          layoutMiscellaneous.findViewById(R.id.layoutDeleteNote).setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                  showDeleteNoteDialog();
              }
          });
      }
    }

    private void showDeleteNoteDialog(){
        if (dialogDeleteNote==null){
            AlertDialog.Builder builder=new AlertDialog.Builder(CreateNote.this);
            View view=LayoutInflater.from(this).inflate(
                    R.layout.layout_delete_note,(ViewGroup)findViewById(R.id.layoutDeleteNoteContainer)
            );
            builder.setView(view);
            dialogDeleteNote=builder.create();
            if (dialogDeleteNote.getWindow()!=null){
                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.textDeleteNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //
                    FancyToast.makeText(CreateNote.this,"Deleted Successful!",FancyToast.LENGTH_LONG, FancyToast.SUCCESS,false).show();

                    class DeleteNoteTask extends AsyncTask<Void,Void,Void>{

                        @Override
                        protected Void doInBackground(Void... voids) {
                            NotesDatabase.getDatabase(getApplicationContext()).noteDao().deleteNote(alreadyAvailableNote);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            Intent intent=new Intent();
                            intent.putExtra("isNoteDeleted",true);
                            setResult(RESULT_OK,intent);
                            startActivity(new Intent(CreateNote.this,MainActivity.class));
                            finish();
                        }
                    }
                    new DeleteNoteTask().execute();
                }
            });
            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogDeleteNote.dismiss();
                }
            });
        }
        dialogDeleteNote.show();
    }
    private void setSubTitleIndicatorColor(){
        GradientDrawable gradientDrawable=(GradientDrawable) viewSubtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
    }
    private void selectImage(){
        Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(intent,REQUEST_CODE_SELECT_IMAGE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==REQUEST_CODE_STORAGE_PERMISSION&&grantResults.length>0)
        {
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                selectImage();
            }else {
                FancyToast.makeText(this,"Permission Denied!",FancyToast.LENGTH_LONG, FancyToast.ERROR,false).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_CODE_SELECT_IMAGE&&resultCode==RESULT_OK){
            if (data!=null){
                Uri selectedImageUri=data.getData();
                if (selectedImageUri !=null){
                    try {
                        InputStream inputStream=getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                        ImageNote.setImageBitmap(bitmap);
                        ImageNote.setVisibility(View.VISIBLE);
                        findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);
                        selectedImagePath=getPathFromUri(selectedImageUri);
                    }catch (Exception exception)
                    {
                        FancyToast.makeText(this,exception.getMessage(),FancyToast.LENGTH_LONG, FancyToast.ERROR,false).show();
                    }
                }
            }
        }
    }
    private String getPathFromUri(Uri contentUri){
        String filePath;
        Cursor cursor=getContentResolver().query(contentUri,null,null,null);
        if(cursor==null)
        {
            filePath=contentUri.getPath();
        }else {
            cursor.moveToFirst();
            int index=cursor.getColumnIndex("_data");
            filePath=cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }
    private void showUrlDialog()
    {
        if (dialogAddURL==null)
        {
            AlertDialog.Builder builder=new AlertDialog.Builder(CreateNote.this);
            View view= LayoutInflater.from(this).inflate(
                    R.layout.layout_add_link,
                    (ViewGroup)findViewById(R.id.layoutAddUrlContainer)
            );
            builder.setView(view);
            dialogAddURL=builder.create();
            if (dialogAddURL.getWindow()!=null){
                dialogAddURL.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            final TextInputEditText inputLink=view.findViewById(R.id.inputLink);
            inputLink.requestFocus();
            view.findViewById(R.id.textAdd).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (inputLink.getText().toString().trim().equals("")){
                        FancyToast.makeText(CreateNote.this,"Enter Link..",FancyToast.LENGTH_LONG, FancyToast.ERROR,false).show();
                    }else if (!Patterns.WEB_URL.matcher(inputLink.getText().toString().trim()).matches()){
                        FancyToast.makeText(CreateNote.this,"You Enter Valid Link..",FancyToast.LENGTH_LONG, FancyToast.ERROR,false).show();
                    }else
                    {
                        textWeb.setText(inputLink.getText().toString());
                        layoutWebURL.setVisibility(View.VISIBLE);
                        dialogAddURL.dismiss();
                    }
                }
            });
            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogAddURL.dismiss();
                }
            });
        }
        dialogAddURL.show();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(CreateNote.this,MainActivity.class));
        finish();
//        super.onBackPressed();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        startAlarm(c);
    }
    @SuppressLint("NewApi")
    private void startAlarm(Calendar c) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);
        if (c.before(Calendar.getInstance())) {
            c.add(Calendar.DATE, 1);
        }
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
    }
}