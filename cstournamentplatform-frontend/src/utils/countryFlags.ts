/**
 * Converts a country code or name to a flag emoji
 * @param country - Country code (e.g., "US", "BR") or country name (e.g., "United States", "Brazil")
 * @returns Flag emoji or the original country string if not found
 */
export function getCountryFlag(country: string | undefined): string {
    if (!country) return '';
    
    // Country code to name mapping (for common CS2 countries)
    const countryCodeMap: { [key: string]: string } = {
        'us': 'United States',
        'usa': 'United States',
        'br': 'Brazil',
        'bra': 'Brazil',
        'dk': 'Denmark',
        'dnk': 'Denmark',
        'se': 'Sweden',
        'swe': 'Sweden',
        'ua': 'Ukraine',
        'ukr': 'Ukraine',
        'ru': 'Russia',
        'rus': 'Russia',
        'de': 'Germany',
        'deu': 'Germany',
        'fr': 'France',
        'fra': 'France',
        'pl': 'Poland',
        'pol': 'Poland',
        'fi': 'Finland',
        'fin': 'Finland',
        'no': 'Norway',
        'nor': 'Norway',
        'tr': 'Turkey',
        'tur': 'Turkey',
        'au': 'Australia',
        'aus': 'Australia',
        'ca': 'Canada',
        'can': 'Canada',
        'kz': 'Kazakhstan',
        'kaz': 'Kazakhstan',
        'rs': 'Serbia',
        'srb': 'Serbia',
        'ba': 'Bosnia and Herzegovina',
        'bih': 'Bosnia and Herzegovina',
        'nl': 'Netherlands',
        'nld': 'Netherlands',
        'be': 'Belgium',
        'bel': 'Belgium',
        'gb': 'United Kingdom',
        'uk': 'United Kingdom',
        'gbr': 'United Kingdom',
        'es': 'Spain',
        'esp': 'Spain',
        'pt': 'Portugal',
        'prt': 'Portugal',
        'it': 'Italy',
        'ita': 'Italy',
        'cz': 'Czech Republic',
        'cze': 'Czech Republic',
        'sk': 'Slovakia',
        'svk': 'Slovakia',
        'bg': 'Bulgaria',
        'bgr': 'Bulgaria',
        'ro': 'Romania',
        'rou': 'Romania',
        'lv': 'Latvia',
        'lva': 'Latvia',
        'lt': 'Lithuania',
        'ltu': 'Lithuania',
        'ee': 'Estonia',
        'est': 'Estonia',
        'cn': 'China',
        'chn': 'China',
        'kr': 'South Korea',
        'kor': 'South Korea',
        'jp': 'Japan',
        'jpn': 'Japan',
        'il': 'Israel',
        'isr': 'Israel',
        'sa': 'Saudi Arabia',
        'sau': 'Saudi Arabia',
        'mn': 'Mongolia',
        'mng': 'Mongolia',
        'hu': 'Hungary',
        'hun': 'Hungary',
        'ar': 'Argentina',
        'arg': 'Argentina',
        'cl': 'Chile',
        'chl': 'Chile',
        'za': 'South Africa',
        'zaf': 'South Africa',
    };

    // Country name to ISO code mapping (for converting names to flag emojis)
    const countryNameToCode: { [key: string]: string } = {
        'united states': 'US',
        'usa': 'US',
        'brazil': 'BR',
        'denmark': 'DK',
        'sweden': 'SE',
        'ukraine': 'UA',
        'russia': 'RU',
        'germany': 'DE',
        'france': 'FR',
        'poland': 'PL',
        'finland': 'FI',
        'norway': 'NO',
        'turkey': 'TR',
        'australia': 'AU',
        'canada': 'CA',
        'kazakhstan': 'KZ',
        'serbia': 'RS',
        'bosnia and herzegovina': 'BA',
        'netherlands': 'NL',
        'united kingdom': 'GB',
        'belgium': 'BE',
        'spain': 'ES',
        'portugal': 'PT',
        'italy': 'IT',
        'czech republic': 'CZ',
        'czechia': 'CZ',
        'slovakia': 'SK',
        'bulgaria': 'BG',
        'romania': 'RO',
        'latvia': 'LV',
        'lithuania': 'LT',
        'estonia': 'EE',
        'china': 'CN',
        'south korea': 'KR',
        'korea': 'KR',
        'japan': 'JP',
        'israel': 'IL',
        'saudi arabia': 'SA',
        'mongolia': 'MN',
        'hungary': 'HU',
        'argentina': 'AR',
        'chile': 'CL',
        'south africa': 'ZA',
    };
    
    const normalized = country.toLowerCase().trim();
    
    // Check if it's a 2-letter code that can be directly converted to flag
    if (normalized.length === 2) {
        const code = normalized.toUpperCase();
        return countryCodeToFlag(code);
    }
    
    // Check if it's a 3-letter code
    if (normalized.length === 3 && countryCodeMap[normalized]) {
        const countryName = countryCodeMap[normalized];
        const code = countryNameToCode[countryName.toLowerCase()];
        if (code) {
            return countryCodeToFlag(code);
        }
    }
    
    // Try to find by country name
    const code = countryNameToCode[normalized];
    if (code) {
        return countryCodeToFlag(code);
    }
    
    // If nothing matches, return original string
    return country;
}

/**
 * Converts an ISO 3166-1 alpha-2 country code to a flag emoji
 * @param code - Two-letter country code (e.g., "US", "BR")
 * @returns Flag emoji
 */
function countryCodeToFlag(code: string): string {
    // Convert country code to regional indicator symbols (flag emoji)
    // Each letter is converted to its regional indicator symbol
    // A = U+1F1E6, B = U+1F1E7, etc.
    return code
        .toUpperCase()
        .split('')
        .map(char => String.fromCodePoint(0x1F1E6 + char.charCodeAt(0) - 65))
        .join('');
}
