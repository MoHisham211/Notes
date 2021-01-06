package mo.zain.notes.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import mo.zain.notes.dao.NoteDao;
import mo.zain.notes.entities.Note;

@Database(entities = Note.class ,version =1,exportSchema = false)
public abstract class NotesDatabase extends RoomDatabase {

    private static NotesDatabase notesDatabase;
    public static synchronized NotesDatabase getDatabase(Context context){
        if (notesDatabase==null)
        {
            notesDatabase= Room.databaseBuilder(
                    context,
                    NotesDatabase.class,
                    "notes_db"
            ).build();

        }
        return notesDatabase;
    }

    public abstract NoteDao noteDao();

    @NonNull
    @Override
    protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration config) {
        return null;
    }

    @NonNull
    @Override
    protected InvalidationTracker createInvalidationTracker() {
        return null;
    }

    @Override
    public void clearAllTables() {

    }
}
