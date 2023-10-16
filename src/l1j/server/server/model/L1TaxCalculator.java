/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package l1j.server.server.model;

public class L1TaxCalculator {
	/**
	 * ���＼��15% ����
	 */
	private static final int WAR_TAX_RATES = 0;

	/**
	 * ������10% ����(�������� ���� ����)
	 */
	private static final int NATIONAL_TAX_RATES = 3;

	/**
	 * ��Ƶ弼��10% ����(���＼�� ���� ����)
	 */
	private static final int DIAD_TAX_RATES = 5;

	private final int _taxRatesCastle;
	private final int _taxRatesTown;
	private final int _taxRatesWar = WAR_TAX_RATES;

	/**
	 * @param merchantNpcId
	 *            ��� ��� ������ NPCID
	 */
	public L1TaxCalculator(int merchantNpcId) {
		_taxRatesCastle = L1CastleLocation.getCastleTaxRateByNpcId(merchantNpcId);
		_taxRatesTown = L1TownLocation.getTownTaxRateByNpcid(merchantNpcId);
	}
	
	public int calcTotalTaxPrice(int price) {
		int taxCastle = (price * _taxRatesCastle) / 100;
		int taxTown = (price * _taxRatesTown) / 100;
		int taxWar = (price * WAR_TAX_RATES) / 100;
		return taxCastle + taxTown + taxWar;		
	}
	// XXX ���������� ����ϱ� ������(����), �ձ� ������ ���´�.
	public int calcCastleTaxPrice(int price) {
		return (price * _taxRatesCastle) / 100 - calcNationalTaxPrice(price);
	}

	public int calcNationalTaxPrice(int price) {
		return (price * _taxRatesCastle) / 100 / (100 / NATIONAL_TAX_RATES);
	}

	public int calcTownTaxPrice(int price) {
		return (price * _taxRatesTown) / 100;
	}

	public int calcWarTaxPrice(int price) {
		return (price * _taxRatesWar) / 100;
	}

	public int calcDiadTaxPrice(int price) {
		return (price * _taxRatesWar) / 100 / (100 / DIAD_TAX_RATES);
	}

	/**
	 * ���� ���� ������ �䱸�Ѵ�.
	 * 
	 * @param price
	 *            �������� ����
	 * @return ���� ���� ����
	 */
	public int layTax(int price) {
		return price + calcTotalTaxPrice(price);
	}
	
	/**
	 * ���� ���� NPC�� ���� �⺻���� �ΰ�  
	 * @param price
	 *            ���ݺΰ� �� ����
	 * @return ���� ��ȭ �� ����
	 */
	public int NoTaxPrice(int price) {
		return price + calcWarTaxPrice(price);
	}
}
