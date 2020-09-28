package emetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConforme;
import transmetteurs.Transmetteur;

/**
 * Class that defines the component emitter. The input type is a Boolean and the output type is a Float. The class
 * implements the class Transmetteur.
 *
 * @author Thierry JIAO - Samy BENCHAREF - Thanh Huy LE - Milo THIBAUD - Lucas BERENGUER
 */
public class Emetteur extends Transmetteur<Boolean, Float> {

    private final String codeType;
    private final Float ampliMax;
    private final Float ampliMin;
    private final Integer nbEchTpsBit;

    /**
     * The constructor sets the attributes in order to convert the information.
     *
     * @param codetype    (String) code type desired to convert the information
     * @param nbEchTpsBit (Integer) number of samples per bit
     * @param ampliMax    (Float) Value max of the amplitude from the signal
     * @param ampliMin    (Float) Value min of the amplitude from the signal
     */
    public Emetteur(String codetype, Integer nbEchTpsBit, Float ampliMax, Float ampliMin) {
        super();
        this.codeType = codetype;
        this.nbEchTpsBit = nbEchTpsBit;
        this.ampliMax = ampliMax;
        this.ampliMin = ampliMin;
    }

    /**
     * Gets the Boolean information and stores in his attribute informationRecue. Then, convert the information to Float
     * and calls the method emettre().
     *
     * @param information (Information<Boolean>) Information to convert
     */
    @Override
    public void recevoir(Information<Boolean> information) throws InformationNonConforme {
        this.informationRecue = information;
        switch (codeType) {
            case "NRZ":
                this.informationEmise = logicToNrz(this.informationRecue);
                break;
            case "NRZT":
                this.informationEmise = logicToNrzt(this.informationRecue);
                break;
            case "RZ":
                this.informationEmise = logicToRz(this.informationRecue);
                break;
            default:
                throw new InformationNonConforme("Wrong Code Type given : " + codeType);
        }
        emettre();
    }

    /**
     * Emits information to all his interfaces in the attribute destinationsConnectees
     */
    @Override
    public void emettre() throws InformationNonConforme {
        // Send the message to the others destinations
        for (DestinationInterface<Float> destinationConnectee : destinationsConnectees) {
            destinationConnectee.recevoir(this.informationEmise);
        }
    }

    /**
     * Converts and samples the logical information into analogical information coded in NRZ (Non Return to Zero)
     *
     * @param information (Information<Boolean>) Logical Information that contains the data
     * @return Information<Float>
     */
    private Information<Float> logicToNrz(Information<Boolean> information) {
        // Construct the new Information<Float>
        Information<Float> newInformation = new Information<>();
        // Iterate through the variable Information<Boolean>
        for (int i = 0; i < information.nbElements(); i++) {
            // If the element has the boolean value 'true', add nbEchTpsBit times a element (at value ampliMax) to the new information
            if (information.iemeElement(i)) {
                newInformation.addManyTimes(ampliMax, nbEchTpsBit);
            } // Otherwise, add nbEchTpsBit times a element (at value ampliMin) to the new information
            else {
                newInformation.addManyTimes(ampliMin, nbEchTpsBit);
            }
        }
        return newInformation;
    }

    /**
     * Converts and samples the logical information into analogical information coded in NRZT (Non Return to Zero
     * trapeze)
     *
     * @param information (Information<Boolean>) Logical Information that contains the data
     * @return Information<Float>
     */
    private Information<Float> logicToNrzt(Information<Boolean> information) {
        // Construct the new Information<Float>
        Information<Float> newInformation = new Information<>();
        // Iterate through the variable Information<Boolean>
        for (int i = 0; i < information.nbElements(); i++) {
            for (int n = 0; n < nbEchTpsBit; n++) {
                if ((i == 0 || !information.iemeElement(i - 1).equals(information.iemeElement(i))) && n < nbEchTpsBit / 3) {
                    if (information.iemeElement(i)) {
                        newInformation.add((n * ampliMax) / (nbEchTpsBit / 3.00f));
                    } else {
                        newInformation.add((n * ampliMin) / (nbEchTpsBit / 3.00f));
                    }
                } else if ((i == information.nbElements() - 1 || !information.iemeElement(i + 1).equals(information.iemeElement(i))) && n > (2 * nbEchTpsBit / 3)) {
                    if (information.iemeElement(i)) {
                        newInformation.add(ampliMax * (3 * (nbEchTpsBit - n)) / nbEchTpsBit);
                    } else {
                        newInformation.add(ampliMin * (3 * (nbEchTpsBit - n)) / nbEchTpsBit);
                    }
                } else {
                    if (information.iemeElement(i)) {
                        newInformation.add(ampliMax);
                    } else {
                        newInformation.add(ampliMin);
                    }
                }
            }
        }
        return newInformation;

    }

    /**
     * Converts and samples the logical information into analogical information coded in RZ (Return to Zero)
     *
     * @param information (Information<Boolean>) Logical Information that contains the data
     * @return Information<Float>
     */
    private Information<Float> logicToRz(Information<Boolean> information) {
        // Construct the new Information<Float>
        Information<Float> newInformation = new Information<>();
        // Iterate through the variable Information<Boolean>
        for (int i = 0; i < information.nbElements(); i++) {
            if (information.iemeElement(i)) {
                for (int n = 0; n < nbEchTpsBit / 3; n++) {
                    newInformation.add(0.00f);
                }
                for (int n = nbEchTpsBit / 3; n < 2 * nbEchTpsBit / 3; n++) {
                    newInformation.add(ampliMax);
                }
                for (int n = 2 * nbEchTpsBit / 3; n < nbEchTpsBit; n++) {
                    newInformation.add(0.00f);
                }
            } else {
                for (int n = 0; n < nbEchTpsBit; n++) {
                    newInformation.add(0.00f);
                }
            }
        }

        return newInformation;

    }
}
