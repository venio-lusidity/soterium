package com.lusidity.test;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;

public class RandomNames {
    private static final Collection<String> NAMES = new ArrayList<>();
    static {
        RandomNames.NAMES.add("Sacheen Klingenschmitt");
        RandomNames.NAMES.add("Bel Brandolini");
        RandomNames.NAMES.add("Spark Calamia");
        RandomNames.NAMES.add("Gyasi Perlac");
        RandomNames.NAMES.add("Nutu Cupec");
        RandomNames.NAMES.add("Oreka Schwamborn");
        RandomNames.NAMES.add("Arttu Koyal");
        RandomNames.NAMES.add("Tamisha Rostaing");
        RandomNames.NAMES.add("Apphia Gorniok");
        RandomNames.NAMES.add("Jonjon Kalda");
        RandomNames.NAMES.add("Emanuel Chval");
        RandomNames.NAMES.add("Bertel Methenitis");
        RandomNames.NAMES.add("Guada Gong");
        RandomNames.NAMES.add("Perkasa Kaethler");
        RandomNames.NAMES.add("Sendi Oreyma");
        RandomNames.NAMES.add("Keirnyn Nickless");
        RandomNames.NAMES.add("Tifanny Magodo");
        RandomNames.NAMES.add("Stanislao Rützler");
        RandomNames.NAMES.add("Arciolinda Gantillon");
        RandomNames.NAMES.add("Uladzimir Losa");
        RandomNames.NAMES.add("Dragoslava Louie");
        RandomNames.NAMES.add("Mozarteum Dittner");
        RandomNames.NAMES.add("Abiya Lizde");
        RandomNames.NAMES.add("Boril Ehrlicher");
        RandomNames.NAMES.add("Francha Zreik");
        RandomNames.NAMES.add("Hanny Enslow");
        RandomNames.NAMES.add("Pharoah Perbatasari");
        RandomNames.NAMES.add("Patrik Talero");
        RandomNames.NAMES.add("Jizhong Oczlon");
        RandomNames.NAMES.add("Perica Cury");
        RandomNames.NAMES.add("Neftun Winopal");
        RandomNames.NAMES.add("Giovannella Auba");
        RandomNames.NAMES.add("Mitsurô Farhang");
        RandomNames.NAMES.add("Leorra Chaitalli");
        RandomNames.NAMES.add("Cobe Riddim");
        RandomNames.NAMES.add("Nadar Mini");
        RandomNames.NAMES.add("Ashelyn Melzer");
        RandomNames.NAMES.add("Santico Brodsky");
        RandomNames.NAMES.add("Menflorante Neubeck");
        RandomNames.NAMES.add("Meny Ollinger");
        RandomNames.NAMES.add("Damarques Hofele");
        RandomNames.NAMES.add("Shebbac Iwaki");
        RandomNames.NAMES.add("Josiara Isman");
        RandomNames.NAMES.add("Sudarat Fourastie");
        RandomNames.NAMES.add("Dóra Healey");
        RandomNames.NAMES.add("Penda Tuomas");
        RandomNames.NAMES.add("Gordan Mialaret");
        RandomNames.NAMES.add("Jocko Pek");
        RandomNames.NAMES.add("Viena Culligan");
        RandomNames.NAMES.add("Syndra Gooley");
        RandomNames.NAMES.add("Yasutoshi Coumare");
        RandomNames.NAMES.add("Khokon Priestley");
        RandomNames.NAMES.add("Tifni Deruvo");
        RandomNames.NAMES.add("Shinzoku Wilck");
        RandomNames.NAMES.add("Adera Eggimann");
        RandomNames.NAMES.add("Efthymios Petra");
        RandomNames.NAMES.add("Alicja Dashow");
        RandomNames.NAMES.add("Latreese Ino");
        RandomNames.NAMES.add("Fredrick Magedman");
        RandomNames.NAMES.add("Ratheesh Pacífico");
        RandomNames.NAMES.add("Tanzila Arverbro");
        RandomNames.NAMES.add("Savina Baledón");
        RandomNames.NAMES.add("Samay Deathe");
        RandomNames.NAMES.add("Takamichi Gattanella");
        RandomNames.NAMES.add("Jokko Ezzemour");
        RandomNames.NAMES.add("Olcun Sahota");
        RandomNames.NAMES.add("Preben Filipov");
        RandomNames.NAMES.add("Gianfilippo Cruok");
        RandomNames.NAMES.add("Chrystelle Sarnow");
        RandomNames.NAMES.add("Ardis Fusselman");
        RandomNames.NAMES.add("Birgir Bellantonio");
        RandomNames.NAMES.add("Jeen Evidy");
        RandomNames.NAMES.add("Ziga Buffham");
        RandomNames.NAMES.add("Masuyo Calvan");
        RandomNames.NAMES.add("Gurcharan Jeayes");
        RandomNames.NAMES.add("Miljana Dupleasis");
        RandomNames.NAMES.add("Curro Paploray");
        RandomNames.NAMES.add("Kritapas Groleau");
        RandomNames.NAMES.add("Elenio Schmelzer");
        RandomNames.NAMES.add("Vesna Neider");
        RandomNames.NAMES.add("Henric Hambourg");
        RandomNames.NAMES.add("Indre Ensslen");
        RandomNames.NAMES.add("Shû Sarabi");
        RandomNames.NAMES.add("Yorgos Cinaroglu");
        RandomNames.NAMES.add("Soula Michalojko");
        RandomNames.NAMES.add("Dominque Babbidge");
        RandomNames.NAMES.add("Sayumi Clodet");
        RandomNames.NAMES.add("Xavy Pomianowski");
        RandomNames.NAMES.add("Chisato Riggas");
        RandomNames.NAMES.add("Ider Szameitat");
        RandomNames.NAMES.add("Bibiane Sencio");
        RandomNames.NAMES.add("Hassane Ohzora");
        RandomNames.NAMES.add("Furiten Swietlik");
        RandomNames.NAMES.add("Jasse Vringova");
        RandomNames.NAMES.add("Asiyah Coombes");
        RandomNames.NAMES.add("Lucrecia Siemaszkowa");
        RandomNames.NAMES.add("Pomy Bertaccini");
        RandomNames.NAMES.add("Keren Stuhlmacher");
        RandomNames.NAMES.add("Kegham Sandschneider");
        RandomNames.NAMES.add("Jaspal Seccafieno");
    }

    public static String at(int index){
        return (index>=0 && index<RandomNames.NAMES.size()) ?  (String)CollectionUtils.get(RandomNames.NAMES, index) : null;
    }
}
