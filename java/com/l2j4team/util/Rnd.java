/*
 * Decompiled with CFR 0.145.
 */
package com.l2j4team.util;

import java.security.SecureRandom;
import java.util.Random;

public final class Rnd
{
	private static final long ADDEND = 11L;
	private static final long MASK = 0xFFFFFFFFFFFFL;
	private static final long MULTIPLIER = 25214903917L;
	private static final RandomContainer rnd = Rnd.newInstance(RandomType.UNSECURE_THREAD_LOCAL);
	protected static volatile long SEED_UNIQUIFIER = 8682522807148012L;
	
	public static final Random directRandom()
	{
		return rnd.directRandom();
	}
	
	public static final double get()
	{
		return rnd.nextDouble();
	}
	
	public static final int get(int n)
	{
		return rnd.get(n);
	}
	
	public static final int get(int min, int max)
	{
		return rnd.get(min, max);
	}
	
	public static final long get(long min, long max)
	{
		return rnd.get(min, max);
	}
	
	public static final RandomContainer newInstance(RandomType type)
	{
		switch (type)
		{
			case UNSECURE_ATOMIC:
			{
				return new RandomContainer(new Random());
			}
			case UNSECURE_VOLATILE:
			{
				return new RandomContainer(new NonAtomicRandom());
			}
			case UNSECURE_THREAD_LOCAL:
			{
				return new RandomContainer(new ThreadLocalRandom());
			}
			case SECURE:
			{
				return new RandomContainer(new SecureRandom());
			}
		}
		throw new IllegalArgumentException();
	}
	
	public static final boolean nextBoolean()
	{
		return rnd.nextBoolean();
	}
	
	public static final void nextBytes(byte[] array)
	{
		rnd.nextBytes(array);
	}
	
	public static final double nextDouble()
	{
		return rnd.nextDouble();
	}
	
	public static final float nextFloat()
	{
		return rnd.nextFloat();
	}
	
	public static final double nextGaussian()
	{
		return rnd.nextGaussian();
	}
	
	public static final int nextInt()
	{
		return rnd.nextInt();
	}
	
	public static final long nextLong()
	{
		return rnd.nextLong();
	}
	
	public static boolean chance(int chance)
	{
		return chance >= 1 && (chance > 99 || rnd.nextInt() + 1 <= chance);
	}
	
	public static boolean chance(double chance)
	{
		return chance >= 1.0 && (chance > 99.0 || rnd.nextInt() + 1 <= chance);
	}
	
	public static final class ThreadLocalRandom extends Random
	{
		private static final long serialVersionUID = 1L;
		private final ThreadLocal<Seed> _seedLocal;
		
		public ThreadLocalRandom()
		{
			_seedLocal = new ThreadLocal<>()
			{
				
				@Override
				public final Seed initialValue()
				{
					return new Seed(++SEED_UNIQUIFIER + System.nanoTime());
				}
			};
		}
		
		public ThreadLocalRandom(final long seed)
		{
			_seedLocal = new ThreadLocal<>()
			{
				
				@Override
				public final Seed initialValue()
				{
					return new Seed(seed);
				}
			};
		}
		
		@Override
		public final int next(int bits)
		{
			return _seedLocal.get().next(bits);
		}
		
		@Override
		public final synchronized void setSeed(long seed)
		{
			if (_seedLocal != null)
			{
				_seedLocal.get().setSeed(seed);
			}
		}
		
		private static final class Seed
		{
			long _seed;
			
			Seed(long seed)
			{
				setSeed(seed);
			}
			
			final int next(int bits)
			{
				_seed = _seed * MULTIPLIER + ADDEND & MASK;
				return (int) (_seed >>> 48 - bits);
			}
			
			final void setSeed(long seed)
			{
				_seed = (seed ^ MULTIPLIER) & MASK;
			}
		}
		
	}
	
	public static enum RandomType
	{
		SECURE,
		UNSECURE_ATOMIC,
		UNSECURE_THREAD_LOCAL,
		UNSECURE_VOLATILE;
	
	}
	
	public static final class RandomContainer
	{
		private final Random _random;
		
		protected RandomContainer(Random random)
		{
			_random = random;
		}
		
		public final Random directRandom()
		{
			return _random;
		}
		
		public final double get()
		{
			return _random.nextDouble();
		}
		
		public final int get(int n)
		{
			return (int) (_random.nextDouble() * n);
		}
		
		public final int get(int min, int max)
		{
			return min + (int) (_random.nextDouble() * (max - min + 1));
		}
		
		public final long get(long min, long max)
		{
			return min + (long) (_random.nextDouble() * (max - min + 1L));
		}
		
		public final boolean nextBoolean()
		{
			return _random.nextBoolean();
		}
		
		public final void nextBytes(byte[] array)
		{
			_random.nextBytes(array);
		}
		
		public final double nextDouble()
		{
			return _random.nextDouble();
		}
		
		public final float nextFloat()
		{
			return _random.nextFloat();
		}
		
		public final double nextGaussian()
		{
			return _random.nextGaussian();
		}
		
		public final int nextInt()
		{
			return _random.nextInt();
		}
		
		public final long nextLong()
		{
			return _random.nextLong();
		}
	}
	
	public static final class NonAtomicRandom extends Random
	{
		private static final long serialVersionUID = 1L;
		private volatile long _seed;
		
		public NonAtomicRandom()
		{
			this(++SEED_UNIQUIFIER + System.nanoTime());
		}
		
		public NonAtomicRandom(long seed)
		{
			setSeed(seed);
		}
		
		@Override
		public final int next(int bits)
		{
			_seed = _seed * MULTIPLIER + ADDEND & MASK;
			return (int) (_seed >>> 48 - bits);
		}
		
		@Override
		public final synchronized void setSeed(long seed)
		{
			_seed = (seed ^ MULTIPLIER) & MASK;
		}
	}
	
}
