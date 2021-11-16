#! /bin/sh

# use of -a preserves order, -A doesn't manager it
declare -a arr

# list of service names, in an array
arr=( "service1" "service2" "service3" "service4" "service5" "service6" )
for svc in "${!arr[@]}"; do 
printf '%s: %s\n' "${svc}" "${arr[${svc}]}";
echo  "pulling from ~/git/${arr[${svc}]}"
cd ~/git/${arr[${svc}]}
git pull
sleep 10
echo "done"
done

### To Run in Gitbash use `$ sh ~/Linux_simple_git_bash_pull.sh`
