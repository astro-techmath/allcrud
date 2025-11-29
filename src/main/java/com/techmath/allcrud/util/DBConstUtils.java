package com.techmath.allcrud.util;

/**
 * Utility class containing constants used for naming database structures
 * such as table names, foreign keys, and sequences.
 * <p>
 * This class is non-instantiable and provides naming conventions to
 * ensure consistency across the codebase and database schema.
 *
 * <p> Example usages:
 * <ul>
 *   <li>{@code DBConstUtils.AUD + "created_by"}</li>
 *   <li>{@code DBConstUtils.FK + "user_id"}</li>
 *   <li>{@code "user" + DBConstUtils.ID}</li>
 * </ul>
 *
 * @author Matheus Maia
 */
public class DBConstUtils {

    private DBConstUtils() {
        throw new IllegalAccessError("Utility class");
    }

    /** Prefix for audit fields (e.g., "aud_created_by"). */
    public static final String AUD = "aud_";

    /** Suffix for ID columns (e.g., "user_id"). */
    public static final String ID = "_id";

    /** Prefix for foreign key constraints (e.g., "fk_user_id"). */
    public static final String FK = "fk_";

    /** Suffix for database sequences (e.g., "user_sequence"). */
    public static final String SEQUENCE = "_sequence";

    /** Suffix for table names (e.g., "user_tb"). */
    public static final String TABLE = "_tb";

}
