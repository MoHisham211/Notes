package mo.zain.notes.adapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import mo.zain.notes.R;
import mo.zain.notes.activities.CreateNote;
import mo.zain.notes.activities.MainActivity;
import mo.zain.notes.database.NotesDatabase;
import mo.zain.notes.entities.Note;
import mo.zain.notes.listeners.NotesListener;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private List<Note> notes;
    private NotesListener notesListener;
    private Timer timer;
    private List<Note> notesSource;


    public NoteAdapter(List<Note> notes,NotesListener notesListener){
        this.notes=notes;
        this.notesListener=notesListener;
        notesSource=notes;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_contaner_note,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        holder.setNote(notes.get(position));
        holder.layoutNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notesListener.onNoteClicked(notes.get(position),position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
    static class NoteViewHolder extends RecyclerView.ViewHolder{
        TextView textTitle,textSubtitle,textDataTime;
        LinearLayout layoutNote;
        RoundedImageView imageNote;
        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle=itemView.findViewById(R.id.textTitle);
            textSubtitle=itemView.findViewById(R.id.SubTitle);
            textDataTime=itemView.findViewById(R.id.textDateTime);
            imageNote=itemView.findViewById(R.id.imageNote);
            layoutNote=itemView.findViewById(R.id.layoutNote);
        }
        private void setNote(Note note){
            String Title=note.getTitle().trim();
            String SubTitle=note.getSubTitle().trim();
            textTitle.setText(Title);
            textSubtitle.setText(SubTitle);
            textDataTime.setText(note.getDateTime().trim());

            GradientDrawable gradientDrawable=(GradientDrawable) layoutNote.getBackground();
            if (note.getColor()!=null)
            {
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            }else {
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }
            if (note.getImagePath()!=null)
            {
                imageNote.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
                imageNote.setVisibility(View.VISIBLE);
            }else
            {
                imageNote.setVisibility(View.GONE);
            }
        }
    }
    public void searchNotes(final String searchKey){
        timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (searchKey.trim().isEmpty()){
                    notes=notesSource;
                }else {
                    ArrayList<Note> temp=new ArrayList<>();
                    for (Note note: notesSource){
                        if (note.getTitle().toLowerCase().contains(searchKey.toLowerCase())
                                ||note.getNoteText().toLowerCase().contains(searchKey.toLowerCase())
                                ||note.getSubTitle().toLowerCase().contains(searchKey.toLowerCase()))
                        {
                            temp.add(note);
                        }
                    }
                    notes=temp;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        },500);
    }
    public void cancelTimer(){
        if (timer!=null){
            timer.cancel();
        }
    }

}