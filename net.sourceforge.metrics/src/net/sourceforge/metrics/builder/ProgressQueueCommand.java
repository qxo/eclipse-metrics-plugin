package net.sourceforge.metrics.builder;

abstract class ProgressQueueCommand {

	public ProgressQueueCommand() {
	}

	abstract void execute();

	boolean isResume() {
		return false;
	}

	boolean isPause() {
		return false;
	}

}
