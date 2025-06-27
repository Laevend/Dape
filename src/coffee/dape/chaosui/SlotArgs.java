package coffee.dape.chaosui;

@FunctionalInterface
public interface SlotArgs<A,B,C>
{
	void populate(A a,B b,C c);
}
