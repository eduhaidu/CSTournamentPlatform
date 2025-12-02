/**
 * Formats a date string to yyyy-mm-dd format
 * Handles various input formats including ISO strings and existing formatted dates
 */
export function formatDate(dateString: string | null | undefined): string {
    if (!dateString) return '';
    
    try {
        // If it's already in yyyy-mm-dd format, return as is
        if (/^\d{4}-\d{2}-\d{2}$/.test(dateString)) {
            return dateString;
        }
        
        // Parse the date and format it
        const date = new Date(dateString);
        
        // Check if date is valid
        if (isNaN(date.getTime())) {
            return dateString; // Return original if can't parse
        }
        
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        
        return `${year}-${month}-${day}`;
    } catch (error) {
        console.error('Error formatting date:', error);
        return dateString;
    }
}

/**
 * Formats a date range with two dates
 */
export function formatDateRange(startDate: string | null | undefined, endDate: string | null | undefined): string {
    const formattedStart = formatDate(startDate);
    const formattedEnd = formatDate(endDate);
    
    if (!formattedStart && !formattedEnd) return '';
    if (!formattedEnd) return formattedStart;
    if (!formattedStart) return formattedEnd;
    
    return `${formattedStart} to ${formattedEnd}`;
}
