package com.example.avendano.cp_scan.Model;

/**
 * Created by Avendano on 6 Mar 2018.
 */

public class Assess_Computers {

        private String model;
        private String mb;
        private String processor;
        private String monitor;
        private String remarks;
        private int scanned;

        public String getRemarks() {
            return remarks;
        }

        public int getScanned() {
            return scanned;
        }


        public String getModel() {
            return model;
        }

        public String getMb() {
            return mb;
        }

        public String getProcessor() {
            return processor;
        }

        public String getMonitor() {
            return monitor;
        }

        public String getRam() {
            return ram;
        }

        public String getKboard() {
            return kboard;
        }

        public String getMouse() {
            return mouse;
        }

        public String getVga() {
            return vga;
        }

        public String getHdd() {
            return hdd;
        }

        public String getStatus() {
            return status;
        }

        public int getComp_id() {
            return comp_id;
        }

        public int getRoom_id() {
            return room_id;
        }

        private String ram;
        private String kboard;

        public Assess_Computers(String model, String mb, String processor, String monitor,
                                String ram, String kboard, String mouse,
                                String vga, String hdd, String status,
                                int comp_id, int pc_no, int scanned) {
            this.model = model;
            this.pc_no = pc_no;
            this.scanned = scanned;
            this.mb = mb;
            this.processor = processor;
            this.monitor = monitor;
            this.ram = ram;
            this.kboard = kboard;
            this.mouse = mouse;
            this.vga = vga;
            this.hdd = hdd;
            this.status = status;
            this.comp_id = comp_id;
        }

        private String mouse;
        private String vga;
        private String hdd;
        private String status;
        private int comp_id;
        private int room_id;

        public int getPc_no() {
            return pc_no;
        }


        private int pc_no;
}
