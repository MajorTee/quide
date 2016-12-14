package io.gitlab.arturbosch.quide.vcs;

import io.gitlab.arturbosch.quide.validation.Validate;

/**
 * @author Artur Bosch
 */
public interface FileChange extends Comparable<FileChange> {

	/**
	 * Enumerates possible types of changes.
	 */
	enum Type {
		ADDITION, REMOVAL, MODIFICATION, RELOCATION
	}

	Type type();

	SourceFile oldFile();

	SourceFile newFile();

	default Patch patch(DiffTool diff) {
		Validate.notNull(diff);
		return diff.createPatchFor(oldFile(), newFile());
	}

	default boolean isOfType(FileChange.Type changeType) {
		Validate.notNull(changeType);
		return type().equals(changeType);
	}
}
