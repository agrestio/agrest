package com.nhl.link.rest.meta.compiler;

import com.nhl.link.rest.meta.LrEntity;

/**
 * @since 1.24
 */
public interface LrEntityCompiler {

	<T> LrEntity<T> compile(Class<T> type, CompilerContext compilerContext);
}
