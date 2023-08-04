package com.scudata.dm;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import com.scudata.cellset.datamodel.PgmCellSet;

/**
 * dfx缓存管理器
 */
public class DfxManager {
	private static DfxManager dfxManager = new DfxManager();
	private HashMap<String, SoftReference<PgmCellSet>> dfxRefMap = 
		new HashMap<String, SoftReference<PgmCellSet>>();

	private DfxManager() {}

	/**
	 * 取dfx缓存管理器实例
	 * @return DfxManager
	 */
	public static DfxManager getInstance() {
		return dfxManager;
	}

	/**
	 * 清除缓存的程序网
	 */
	public void clear() {
		synchronized(dfxRefMap) {
			dfxRefMap.clear();
		}
	}
	
	/**
	 * 使用完dfx，还给缓存管理器
	 * @param dfx PgmCellSet
	 */
	public void putDfx(PgmCellSet dfx) {
		Context dfxCtx = dfx.getContext();
		dfxCtx.setParent(null);
		dfxCtx.setJobSpace(null);
		dfx.reset();

		synchronized(dfxRefMap) {
			dfxRefMap.put(dfx.getName(), new SoftReference<PgmCellSet>(dfx));
		}
	}

	/**
	 * 从缓存管理器中取dfx，使用完后需要调用putDfx还给缓存管理器
	 * @param name dfx文件名
	 * @param ctx 计算上下文
	 * @return PgmCellSet
	 */
	public PgmCellSet removeDfx(String name, Context ctx) {
		PgmCellSet dfx = null;
		synchronized(dfxRefMap) {
			SoftReference<PgmCellSet> sr = dfxRefMap.remove(name);
			if (sr != null) dfx = (PgmCellSet)sr.get();
		}

		if (dfx == null) {
			return readDfx(name, ctx);
		} else {
			// 不再共享ctx中的变量
			Context dfxCtx = dfx.getContext();
			dfxCtx.setEnv(ctx);
			return dfx;
		}
	}

	/**
	 * 从缓存管理器中取dfx，使用完后需要调用putDfx还给缓存管理器
	 * @param fo dfx文件对象
	 * @param ctx 计算上下文
	 * @return PgmCellSet
	 */
	public PgmCellSet removeDfx(FileObject fo, Context ctx) {
		PgmCellSet dfx = null;
		String name = fo.getFileName();
		synchronized(dfxRefMap) {
			SoftReference<PgmCellSet> sr = dfxRefMap.remove(name);
			if (sr != null) dfx = (PgmCellSet)sr.get();
		}
		
		if (dfx == null) {
			return readDfx(fo, ctx);
		} else {
			// 不再共享ctx中的变量
			Context dfxCtx = dfx.getContext();
			dfxCtx.setEnv(ctx);
			return dfx;
		}
	}
	
	/**
	 * 读取dfx，不会使用缓存
	 * @param fo dfx文件对象
	 * @param ctx 计算上下文
	 * @return PgmCellSet
	 */
	public PgmCellSet readDfx(FileObject fo, Context ctx) {
		PgmCellSet dfx = fo.readPgmCellSet();
		dfx.resetParam();
		
		// 不再共享ctx中的变量
		Context dfxCtx = dfx.getContext();
		dfxCtx.setEnv(ctx);
		return dfx;
	}
	
	/**
	 * 读取dfx，不会使用缓存
	 * @param name dfx文件名
	 * @param ctx 计算上下文
	 * @return PgmCellSet
	 */
	public PgmCellSet readDfx(String name, Context ctx) {
		return readDfx(new FileObject(name, null, "s", ctx), ctx);
	}
}
