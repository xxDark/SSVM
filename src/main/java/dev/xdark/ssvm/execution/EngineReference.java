package dev.xdark.ssvm.execution;

interface EngineReference<T> {

	void recycle(T value);
}
