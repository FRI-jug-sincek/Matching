Uporabni ukazi:

kubectl apply -f users-deployment.yaml
kubectl logs -f users-deployment-6679d6bb76-lhtgc
kubectl get pods

kubectl scale deployment users-deployment --replicas=0
kubectl scale deployment users-deployment --replicas=1
