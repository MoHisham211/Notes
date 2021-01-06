package mo.zain.notes.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import mo.zain.notes.R;
import mo.zain.notes.adapter.NoteAdapter;
import mo.zain.notes.database.NotesDatabase;
import mo.zain.notes.entities.Note;
import mo.zain.notes.listeners.NotesListener;

public class MainActivity extends AppCompatActivity implements NotesListener {
    public static final int REQUEST_CODE_ADD_NOTE=1;
    public static final int REQUEST_CODE_UPDATE_NOTE=2;
    public static final int REQUEST_CODE_SHOW_NOTES=3;
    private RecyclerView recyclerView;
    private int noteClickedPosition=-1;
    List<Note> noteList;
    NoteAdapter noteAdapter;
    ImageView imageAddNote,imageDrawing;
    EditText inputSearch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageDrawing=findViewById(R.id.imageAddNote);
        imageDrawing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,DrawingActivity.class));
            }
        });
        imageAddNote=findViewById(R.id.imageAddNoteMain);
        imageAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(getApplicationContext(), CreateNote.class),REQUEST_CODE_ADD_NOTE
                );
                finish();
            }
        });
        recyclerView=findViewById(R.id.notesRecyclerView);
        recyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
        );
        noteList=new ArrayList<>();
        noteAdapter=new NoteAdapter(noteList,this);
        recyclerView.setAdapter(noteAdapter);
        getNotes(REQUEST_CODE_SHOW_NOTES,false);
        inputSearch=findViewById(R.id.inputSearch);
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                noteAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (noteList.size()!=0){
                    noteAdapter.searchNotes(s.toString());
                }
            }
        });
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition=position;
        Intent intent=new Intent(getApplicationContext(),CreateNote.class);
        intent.putExtra("isViewOrUpdate",true);
        intent.putExtra("note",note);
        startActivityForResult(intent,REQUEST_CODE_UPDATE_NOTE);
        finish();
    }

    private void getNotes(final int requestCode,final boolean isNoteDeleted){
        @SuppressLint("StaticFieldLeak")
        class  GetNotesTask extends AsyncTask<Void,Void, List<Note>>{
            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NotesDatabase.getDatabase(getApplicationContext())
                        .noteDao().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                if (requestCode==REQUEST_CODE_SHOW_NOTES){
                    noteList.addAll(notes);
                    noteAdapter.notifyDataSetChanged();
                }else if (requestCode==REQUEST_CODE_ADD_NOTE){
                    noteList.add(0,notes.get(0));
                    recyclerView.smoothScrollToPosition(0);
                }else if (requestCode==REQUEST_CODE_UPDATE_NOTE){
                    noteList.remove(noteClickedPosition);

//                    noteList.add(noteClickedPosition,notes.get(noteClickedPosition));
//                    noteAdapter.notifyItemChanged(noteClickedPosition);

                    if (isNoteDeleted){
                        noteAdapter.notifyItemRemoved(noteClickedPosition);
                    }else {
                        noteList.add(noteClickedPosition,notes.get(noteClickedPosition));
                        noteAdapter.notifyItemChanged(noteClickedPosition);
                    }
                }

                }
        }
        new GetNotesTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_CODE_ADD_NOTE && resultCode==RESULT_OK){
            getNotes(REQUEST_CODE_SHOW_NOTES,false);
        }
        else if(requestCode==REQUEST_CODE_UPDATE_NOTE && resultCode==RESULT_OK) {
            if (data !=null){
                getNotes(REQUEST_CODE_UPDATE_NOTE,data.getBooleanExtra("isNoteDeleted",false));
            }
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        System.exit(0);
    }
}