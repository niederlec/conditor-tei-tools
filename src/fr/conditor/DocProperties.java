package fr.conditor;

import java.io.File;

/**
 * Created by niederle on 01/11/17.
 */


public class DocProperties {
    File xmlFile;
    File teiFile;
    String idIstex;
    String arkIstex;
    File issueXmlFile;
    File txtFile;

    public DocProperties(String idIstex) {
        this.idIstex = idIstex;
    }

    public DocProperties(File xml, File tei, String idIstex) {
        this.xmlFile = xml;
        this.teiFile = tei;
        this.idIstex = idIstex;
    }

    public File getIssueXmlFile() {
        return issueXmlFile;
    }

    public void setIssueXmlFile(File issueXmlFile) {
        this.issueXmlFile = issueXmlFile;
    }

    public String getArkIstex() {
        return arkIstex;
    }

    public void setArkIstex(String arkIstex) {
        this.arkIstex = arkIstex;
    }

    public File getTxtFile() {
        return txtFile;
    }

    public void setTxtFile(File txtFile) {
        this.txtFile = txtFile;
    }

    public File getTeiFile() {
        return teiFile;
    }

    public void setTeiFile(File teiFile) {
        this.teiFile = teiFile;
    }

    public File getXmlFile() {
        return xmlFile;
    }

    public void setXmlFile(File xmlFile) {
        this.xmlFile = xmlFile;
    }
}
