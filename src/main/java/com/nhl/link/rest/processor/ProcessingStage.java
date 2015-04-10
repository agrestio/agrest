package com.nhl.link.rest.processor;

/**
 * A single stage in the chain of responsibility of select request processing.
 * 
 * @since 1.16
 */
public abstract class ProcessingStage<C extends ProcessingContext<T>, T> implements Processor<C, T> {

	private Processor<C, ? super T> next;

	public ProcessingStage(Processor<C, ? super T> next) {
		this.next = next;
	}

	/**
	 * Executes this stage and then delegates to the next Processor in the chain
	 * of responsibility. You should override
	 * {@link #doExecute(ProcessingContext)} to implement own execution, and
	 * override this method to change the default stage flow.
	 */
	public void execute(C context) {

		// run our own processing, and then pass control down the chain...
		doExecute(context);

		if (next != null) {
			next.execute(context);
		}
	}

	protected abstract void doExecute(C context);
}
