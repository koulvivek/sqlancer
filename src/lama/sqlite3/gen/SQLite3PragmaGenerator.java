package lama.sqlite3.gen;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

import lama.Main.StateToReproduce;
import lama.QueryAdapter;
import lama.Randomly;

public class SQLite3PragmaGenerator {

	private enum Pragma {
		APPLICATION_ID, AUTO_VACUUM, AUTOMATIC_INDEX, BUSY_TIMEOUT, CACHE_SIZE, CACHE_SPILL_ENABLED, CACHE_SPILL_SIZE,
		// CASE_SENSITIVE_LIKE, // see
		// https://www.mail-archive.com/sqlite-users@mailinglists.sqlite.org/msg115030.html
		CELL_SIZE_CHECK, CHECKPOINT_FULLSYNC, DEFER_FOREIGN_KEY, /* ENCODING, */FOREIGN_KEYS, IGNORE_CHECK_CONSTRAINTS,
		INCREMENTAL_VACUUM, INTEGRITY_CHECK, JOURNAL_MODE, JOURNAL_SIZE_LIMIT, /* LEGACY_ALTER_TABLE , */ OPTIMIZE,
		LEGACY_FORMAT, REVERSE_UNORDERED_SELECTS, SECURE_DELETE, SHRINK_MEMORY, SOFT_HEAP_LIMIT, THREADS
	}

	private final StringBuilder sb = new StringBuilder();

	public void createPragma(String pragmaName, Supplier<Object> supplier) {
		boolean setSchema = Randomly.getBoolean();
		boolean setValue = Randomly.getBoolean();
		sb.append("PRAGMA ");
		if (setSchema) {
			sb.append(Randomly.fromOptions("main.", "temp."));
		}
		sb.append(pragmaName);
		if (setValue) {
			Object value = supplier.get();
			if (value != null) {
				sb.append(" = ");
				sb.append(supplier.get());
			}
		}
	}

	public QueryAdapter insert(Connection con, StateToReproduce state, Randomly r) {
		Pragma p = Randomly.fromOptions(Pragma.values());
		switch (p) {
		case APPLICATION_ID:
			createPragma("application_id", () -> r.getInteger());
			break;
		case AUTO_VACUUM:
			createPragma("auto_vacuum", () -> Randomly.fromOptions("NONE", "FULL", "INCREMENTAL"));
			break;
		case AUTOMATIC_INDEX:
			createPragma("automatic_index", () -> getRandomTextBoolean());
			break;
		case BUSY_TIMEOUT:
			createPragma("busy_timeout", () -> {
				if (Randomly.getBoolean()) {
					return 0;
				} else {
					long value = Math.max(10000, r.getInteger());
					return value;
				}

			});
			break;
		case CACHE_SIZE:
			createPragma("cache_size", () -> {
				if (Randomly.getBoolean()) {
					return 0;
				} else {
					return r.getInteger();
				}
			});
			break;
		case CACHE_SPILL_ENABLED:
			createPragma("cache_spill", () -> getRandomTextBoolean());
			break;
		case CACHE_SPILL_SIZE:
			createPragma("cache_spill", () -> r.getInteger());
			break;
		case CELL_SIZE_CHECK:
			createPragma("cell_size_check", () -> getRandomTextBoolean());
			break;
		case CHECKPOINT_FULLSYNC:
			createPragma("checkpoint_fullfsync", () -> getRandomTextBoolean());
			break;
		case DEFER_FOREIGN_KEY:
			createPragma("defer_foreign_keys", () -> getRandomTextBoolean());
			break;
		// TODO: [SQLITE_ERROR] SQL error or missing database (attached databases must
		// use the same text encoding as main database)
//	case ENCODING:
//		sb.append("PRAGMA main.encoding = \"");
//		String encoding = Randomly.fromOptions("UTF-8", "UTF-16", "UTF-16be", "UTF-16le");
//		sb.append(encoding);
//		sb.append("\";\n");
//		sb.append("PRAGMA temp.encoding = \"");
//		sb.append(encoding);
//		sb.append("\";");
//		break;
		case FOREIGN_KEYS:
			createPragma("foreign_keys", () -> getRandomTextBoolean());
			break;
		case IGNORE_CHECK_CONSTRAINTS:
			createPragma("ignore_check_constraints", () -> getRandomTextBoolean());
			break;
		case INCREMENTAL_VACUUM:
			if (Randomly.getBoolean()) {
				createPragma("incremental_vacuum", () -> null);
			} else {
				sb.append(String.format("PRAGMA incremental_vacuum(%d)", r.getInteger()));
			}
			break;
		case INTEGRITY_CHECK:
			if (Randomly.getBoolean()) {
				createPragma("integrity_check", () -> null);
			} else {
				sb.append(String.format("PRAGMA integrity_check(%d)", r.getInteger()));
			}
			break;
		case JOURNAL_MODE:
			// OFF is no longer generated, since it might corrupt the database upon failed
			// index creation, see https://www.sqlite.org/src/tktview?name=f4ec250930.
			createPragma("journal_mode", () -> Randomly.fromOptions("DELETE", "TRUNCATE", "PERSIST", "MEMORY", "WAL"));
			break;
		case JOURNAL_SIZE_LIMIT:
			createPragma("journal_size_limit", () -> {
				if (Randomly.getBoolean()) {
					return 0;
				} else {
					return r.getInteger();
				}

			});
			break;
//	case LEGACY_ALTER_TABLE:
//		sb.append("PRAGMA legacy_alter_table=");
//		sb.append(getRandomTextBoolean());
//		break;
		case LEGACY_FORMAT:
			createPragma("legacy_file_format", () -> getRandomTextBoolean());
			break;
		case OPTIMIZE:
			createPragma("optimize", () -> null);
			break;
		case REVERSE_UNORDERED_SELECTS:
			createPragma("reverse_unordered_selects", () -> getRandomTextBoolean());
			break;
		case SECURE_DELETE:
			createPragma("secure_delete", () -> Randomly.fromOptions("true", "false", "FAST"));
			break;
		case SHRINK_MEMORY:
			createPragma("shrink_memory", () -> null);
			break;
		case SOFT_HEAP_LIMIT:
			createPragma("soft_heap_limit", () -> {
				if (Randomly.getBoolean()) {
					return 0;
				} else {
					return r.getPositiveInteger();
				}
			});
			break;
		case THREADS:
			createPragma("threads", () -> r.getInteger());
			break;
		default:
			throw new AssertionError();
		}
		sb.append(";");
		String pragmaString = sb.toString();
		return new QueryAdapter(pragmaString);
	}

	public static QueryAdapter insertPragma(Connection con, StateToReproduce state, Randomly r) throws SQLException {
		return new SQLite3PragmaGenerator().insert(con, state, r);
	}

	private static String getRandomTextBoolean() {
		return Randomly.fromOptions("true", "false");
	}

}