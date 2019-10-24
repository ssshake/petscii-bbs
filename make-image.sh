cat $1 | od -t dC | sed -E 's/-/ -/g' | sed -E 's/\s+$//g' | sed -E 's/\s+/, /g' | sed -E 's/$/,/' | cut -c9-
echo " 13"
