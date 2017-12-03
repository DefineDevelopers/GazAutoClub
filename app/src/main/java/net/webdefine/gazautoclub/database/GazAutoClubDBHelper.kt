package net.webdefine.gazautoclub.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import net.webdefine.gazautoclub.App
import org.jetbrains.anko.db.*

class GazAutoClubDBHelper(context: Context = App.instance) : ManagedSQLiteOpenHelper(context,
        GazAutoClubDBHelper.DB_NAME, null, GazAutoClubDBHelper.DB_VERSION) {
    companion object {
        val DB_NAME = "gac.db"
        val DB_VERSION = 1
        val instance by lazy { GazAutoClubDBHelper() }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.createTable(PostTable.NAME, true,
                PostTable.ID        to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                PostTable.AUTHOR_ID           to INTEGER,
                PostTable.DATE                to INTEGER,
                PostTable.TITLE               to TEXT,
                PostTable.DESCRIPTION         to TEXT,
                PostTable.FULL_TEXT           to TEXT,
                PostTable.IMAGE_RESOURCE_ID   to TEXT,
                PostTable.LIKES               to INTEGER,
                PostTable.COMMENTS            to INTEGER
        )

        db.createTable(PersonTable.NAME, true,
                PersonTable.ID      to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                PersonTable.FIRST_NAME        to TEXT,
                PersonTable.SECOND_NAME       to TEXT,
                PersonTable.EMAIL             to TEXT,
                PersonTable.PASSWORD          to TEXT,
                PersonTable.POSTS             to TEXT
        )
    }

    fun fillDB(db: SQLiteDatabase) {
        db.insert("Post",
                "_id" to 1,
                "author_id" to 1,
                "name" to "BUIC",
                "description" to "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                "full_text" to "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur porta felis in elit accumsan placerat. Sed ipsum libero, porttitor sit amet lacus vel, venenatis hendrerit purus.",
                "image_res_id" to "@drawable/buic")

        db.insert("Post",
                "_id" to 2,
                "author_id" to 1,
                "name" to "Cherry",
                "description" to "Phasellus ipsum ex, molestie sollicitudin vestibulum eget, venenatis eget mauris.",
                "full_text" to "Phasellus ipsum ex, molestie sollicitudin vestibulum eget, venenatis eget mauris. Pellentesque rhoncus ullamcorper rutrum.",
                "image_res_id" to "@drawable/chry")

        db.insert("Post",
                "_id" to 3,
                "author_id" to 2,
                "name" to "Orange Car",
                "description" to "Nullam consequat quam ligula, in elementum dui venenatis at.",
                "full_text" to "Nullam consequat quam ligula, in elementum dui venenatis at. Vestibulum volutpat nisl diam, semper dignissim nisi malesuada et. Aliquam dapibus erat condimentum faucibus consequat. Nunc dignissim sem quam, in ornare ipsum pulvinar at.",
                "image_res_id" to "@drawable/orange")

        db.insert("Post",
                "_id" to 4,
                "author_id" to 8,
                "name" to "P-Tar",
                "description" to "In eget neque ut orci euismod commodo.",
                "full_text" to "In eget neque ut orci euismod commodo. Curabitur sollicitudin, augue id ultrices posuere, tortor tellus sodales nunc, at interdum eros leo gravida odio.",
                "image_res_id" to "@drawable/ptar")

        db.insert("Post",
                "_id" to 5,
                "author_id" to 16,
                "name" to "Swift",
                "description" to "Aliquam gravida, massa sit amet faucibus ullamcorper, velit orci ullamcorper mauris, non consectetur justo nisl ut arcu.",
                "full_text" to "Aliquam gravida, massa sit amet faucibus ullamcorper, velit orci ullamcorper mauris, non consectetur justo nisl ut arcu. Etiam eu nisi diam.",
                "image_res_id" to "@drawable/swift")

        db.insert("Post",
                "_id" to 6,
                "author_id" to 55,
                "name" to "VauX",
                "description" to "Aenean sed libero vel odio ullamcorper malesuada eget sit amet ante.",
                "full_text" to "Aenean sed libero vel odio ullamcorper malesuada eget sit amet ante. Maecenas eu magna mollis, semper lacus at, tincidunt tellus. Nulla luctus mauris vitae sodales volutpat.",
                "image_res_id" to "@drawable/vaux")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.dropTable(PostTable.NAME, true)
        db.dropTable(PersonTable.NAME, true)
        onCreate(db)
    }
}
// Access property for Context
val Context.database: GazAutoClubDBHelper
    get() = GazAutoClubDBHelper.instance