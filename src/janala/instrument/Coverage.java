/*
 * Copyright (c) 2012, NTT Multimedia Communications Laboratories, Inc. and Koushik Sen
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package janala.instrument;

import janala.config.Config;
import janala.utils.MyLogger;

import java.io.*;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 */
public class Coverage implements Serializable {
    private HashMap<String,Integer> classNameToCid;
    private int nBranches;
    private int nCovered;
    private TreeMap<Integer, Integer> covered;
    private TreeMap<Integer, Integer> tmpCovered;
    private boolean isNewClass;

    public static Coverage instance = null;
    private final static Logger logger = MyLogger.getLogger(Coverage.class.getName());


    private Coverage() {
        classNameToCid = new HashMap<String,Integer>();
        nBranches = 0;
        nCovered = 0;
        covered = new TreeMap<Integer, Integer>();
        tmpCovered = new TreeMap<Integer, Integer>();
    }

    public static void read() {
        if (instance == null) {
            ObjectInputStream inputStream = null;

            try {
                inputStream = new ObjectInputStream(new FileInputStream(Config.instance.coverage));
                Object tmp = inputStream.readObject();
                if (tmp instanceof Coverage) {
                    instance = (Coverage) tmp;
                } else {
                    instance = new Coverage();
                }
            } catch (Exception e) {
                instance = new Coverage();
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException ex) {
                    logger.log(Level.WARNING, "", ex);
                }
            }
        }
    }

    public static void write() {
        ObjectOutputStream outputStream;
        try {
            outputStream = new ObjectOutputStream(new FileOutputStream(Config.instance.coverage));
            instance.tmpCovered.clear();
            outputStream.writeObject(instance);
            outputStream.close();

        } catch (IOException e) {
            logger.log(Level.SEVERE, "", e);
            System.exit(1);
        }

    }


    public int getCid(String cname) {
        int ret;
        if (classNameToCid.containsKey(cname)) {
            isNewClass = false;
            return classNameToCid.get(cname);
        } else {
//            System.out.println(cname);
            classNameToCid.put(cname, ret = classNameToCid.size());
            if (cname.equals("catg/CATG"))
                isNewClass = false;
            else
                isNewClass = true;
            return ret;
        }
    }

    public void addBranchCount(int iid) {
        if (isNewClass) {
            nBranches += 2;
            covered.put(iid, 0);
            //System.out.println(iid);
        }
    }

    public void visitBranch(int iid, boolean side) {
        if (!tmpCovered.containsKey(iid)) {
            tmpCovered.put(iid, 0);
        }
        tmpCovered.put(iid, tmpCovered.get(iid)|(side?1:2));
    }

    public void commitBranches() {
        for (int key : tmpCovered.keySet()) {
            int value = tmpCovered.get(key);
            if (covered.containsKey(key)) {
                int oldValue = covered.get(key);
                covered.put(key, oldValue | value);
                if ((value & 2) > (oldValue & 2)) {
                    nCovered++;
                    //System.out.println(key + " false ");
                }
                if ((value & 1) > (oldValue & 1)) {
                    nCovered++;
                    //System.out.println(key + " true ");
                }
            }
        }
        System.out.println("Coverage "+(100.0*nCovered/nBranches)+"%");
    }

}